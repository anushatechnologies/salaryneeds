package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Address;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.entity.enums.BookingStatusTab;
import com.salaryneeds.exception.*;
import com.salaryneeds.repository.AddressRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.util.GeoDistanceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final CouponService couponService;
    private final PasswordEncoder passwordEncoder;
    private final WorkerMatchingService workerMatchingService;
    private final WorkerProfileRepository workerProfileRepository;
    private final com.salaryneeds.repository.WorkerLocationRepository workerLocationRepository;
    private final NotificationService notificationService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponseDTO> getAvailableSlots(Long serviceId, LocalDate date) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate targetDate = (date != null) ? date : today;
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);

        List<SlotDefinition> definitions = Arrays.asList(
                new SlotDefinition("SLOT-0912", "09:00 AM - 12:00 PM", LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new SlotDefinition("SLOT-1215", "12:00 PM - 03:00 PM", LocalTime.of(12, 0), LocalTime.of(15, 0)),
                new SlotDefinition("SLOT-1518", "03:00 PM - 06:00 PM", LocalTime.of(15, 0), LocalTime.of(18, 0)),
                new SlotDefinition("SLOT-1821", "06:00 PM - 09:00 PM", LocalTime.of(18, 0), LocalTime.of(21, 0))
        );

        List<SlotResponseDTO> slots = new ArrayList<>();

        for (SlotDefinition def : definitions) {
            LocalDateTime slotStart = targetDate.atTime(def.startTime);
            LocalDateTime slotEnd = targetDate.atTime(def.endTime);
            // Instamart-style: Slot remains active and bookable until the slot window ends (e.g., 3:00 PM for 12:00 PM - 03:00 PM)
            LocalDateTime cutoffTime = slotEnd;

            boolean isAvailable = true;
            String message = "Available";

            if (targetDate.isBefore(today)) {
                isAvailable = false;
                message = "Slot date is in the past";
            } else if (targetDate.isEqual(today) && now.isAfter(cutoffTime)) {
                isAvailable = false;
                message = "Booking cutoff time passed for this slot";
            } else if (serviceId != null) {
                // Check capacity: max 5 concurrent bookings per slot for this service
                long bookedCount = bookingRepository.countByServiceIdAndBookingDateAndSlotIdAndStatusNotIn(
                        serviceId, targetDate, def.slotId, List.of(BookingStatus.CANCELLED)
                );
                if (bookedCount >= 5) {
                    isAvailable = false;
                    message = "Slot is fully booked";
                }
            }

            slots.add(SlotResponseDTO.builder()
                    .slotId(def.slotId)
                    .timeRange(def.timeRange)
                    .startTime(def.startTime.toString())
                    .endTime(def.endTime.toString())
                    .isAvailable(isAvailable)
                    .cutoffTime(cutoffTime)
                    .message(message)
                    .build());
        }

        return slots;
    }

    @Override
    public BookingResponseDTO createBooking(BookingCreateRequestDTO request, String customerIdHeader) {
        String effectiveCustomerId = (customerIdHeader != null && !customerIdHeader.isBlank())
                ? customerIdHeader.trim()
                : request.getCustomerId();

        if (effectiveCustomerId == null || effectiveCustomerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required either in X-Customer-Id header or request body");
        }

        // Validate customer if valid UUID
        try {
            UUID custUuid = UUID.fromString(effectiveCustomerId);
            if (!customerRepository.existsById(custUuid)) {
                throw new CustomerNotFoundException("Customer not found with id: " + effectiveCustomerId);
            }
        } catch (IllegalArgumentException e) {
            // Non-UUID customerId string passed from gateway, accept as valid identity
        }

        // Validate Service
        String serviceName = request.getServiceName();
        String categoryId = request.getCategoryId();
        if (request.getServiceId() != null) {
            Optional<ServiceItem> serviceOpt = serviceItemRepository.findById(request.getServiceId());
            if (serviceOpt.isPresent()) {
                ServiceItem service = serviceOpt.get();
                if (serviceName == null || serviceName.isBlank()) {
                    serviceName = service.getName();
                }
                if (categoryId == null || categoryId.isBlank()) {
                    categoryId = service.getCategory() != null ? service.getCategory().getId().toString() : null;
                }
            }
        }

        // Address Summary & Coordinates
        String addressSummary = null;
        Double customerLat = request.getCustomerLat();
        Double customerLng = request.getCustomerLng();
        if (request.getAddressId() != null && !request.getAddressId().isBlank()) {
            try {
                UUID addrUuid = UUID.fromString(request.getAddressId());
                Optional<Address> addrOpt = addressRepository.findById(addrUuid);
                if (addrOpt.isPresent()) {
                    Address addr = addrOpt.get();
                    addressSummary = addr.toFormattedAddress();
                    if (customerLat == null) {
                        customerLat = addr.getLat();
                    }
                    if (customerLng == null) {
                        customerLng = addr.getLng();
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Discount & Coupon Calculation
        BigDecimal totalAmount = request.getTotalAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal payableAmount = totalAmount;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            CouponValidationResponseDTO couponResult = couponService.validateCoupon(
                    request.getCouponCode(), totalAmount, request.getServiceId(), effectiveCustomerId
            );
            if (Boolean.TRUE.equals(couponResult.getValid())) {
                discountAmount = couponResult.getDiscountAmount();
                payableAmount = couponResult.getFinalAmount();
                couponService.recordCouponUsage(request.getCouponCode());
            }
        }

        Booking booking = Booking.builder()
                .customerId(effectiveCustomerId)
                .serviceId(request.getServiceId())
                .serviceName(serviceName)
                .categoryId(categoryId)
                .bookingDate(request.getBookingDate())
                .status(BookingStatus.PENDING)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .payableAmount(payableAmount)
                .addressId(request.getAddressId())
                .addressSummary(addressSummary)
                .customerLat(customerLat)
                .customerLng(customerLng)
                .slotId(request.getSlotId() != null ? request.getSlotId() : "SLOT-0912")
                .scheduledTime(request.getScheduledTime() != null ? request.getScheduledTime() : "09:00 AM - 12:00 PM")
                .couponCode(request.getCouponCode())
                .notes(request.getNotes())
                .startPinHash(null)
                .startPinEncrypted(null)
                .startPinVerified(false)
                .pinAttempts(0)
                .pinExpiresAt(null)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Trigger Worker Matching Engine to generate offers for eligible workers
        try {
            workerMatchingService.matchAndCreateOffers(savedBooking);
        } catch (Exception e) {
            // Log matching error so booking creation is never blocked
        }

        return mapToResponseDTO(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<BookingResponseDTO> getBookings(String customerId, String statusFilter, Pageable pageable) {
        Page<Booking> page;

        if (statusFilter != null && !statusFilter.isBlank()) {
            String filterUpper = statusFilter.trim().toUpperCase();

            // Check if statusFilter is a BookingStatusTab (UPCOMING, ACTIVE, COMPLETED, CANCELLED)
            BookingStatusTab tab = null;
            try {
                tab = BookingStatusTab.valueOf(filterUpper);
            } catch (IllegalArgumentException ignored) {
            }

            if (tab != null) {
                if (customerId != null && !customerId.isBlank()) {
                    page = bookingRepository.findByCustomerIdAndStatusIn(customerId, tab.getStatuses(), pageable);
                } else {
                    page = bookingRepository.findAll(pageable);
                }
            } else {
                // Check if exact BookingStatus enum
                try {
                    BookingStatus exactStatus = BookingStatus.valueOf(filterUpper);
                    if (customerId != null && !customerId.isBlank()) {
                        page = bookingRepository.findByCustomerIdAndStatus(customerId, exactStatus, pageable);
                    } else {
                        page = bookingRepository.findAll(pageable);
                    }
                } catch (IllegalArgumentException e) {
                    if (customerId != null && !customerId.isBlank()) {
                        page = bookingRepository.findByCustomerId(customerId, pageable);
                    } else {
                        page = bookingRepository.findAll(pageable);
                    }
                }
            }
        } else {
            if (customerId != null && !customerId.isBlank()) {
                page = bookingRepository.findByCustomerId(customerId, pageable);
            } else {
                page = bookingRepository.findAll(pageable);
            }
        }

        List<BookingResponseDTO> content = page.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<BookingResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long bookingId, String customerIdHeader) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (customerIdHeader != null && !customerIdHeader.isBlank() && !booking.getCustomerId().equals(customerIdHeader.trim())) {
            throw new BookingNotFoundException("Booking not found with id: " + bookingId + " for customer: " + customerIdHeader);
        }

        return mapToResponseDTO(booking);
    }

    @Override
    public CancelBookingResponseDTO cancelBooking(Long bookingId, BookingCancelRequestDTO request, String customerIdHeader) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (customerIdHeader != null && !customerIdHeader.isBlank() && !booking.getCustomerId().equals(customerIdHeader.trim())) {
            throw new BookingNotFoundException("Booking not found with id: " + bookingId + " for customer: " + customerIdHeader);
        }

        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus == BookingStatus.IN_PROGRESS) {
            throw new InvalidBookingStateException("Cannot cancel a booking that is already IN_PROGRESS");
        }
        if (currentStatus == BookingStatus.COMPLETED) {
            throw new InvalidBookingStateException("Cannot cancel a completed booking");
        }
        if (currentStatus == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking is already CANCELLED");
        }

        // Cancellation Fee & Refund calculation
        BigDecimal cancellationFee = BigDecimal.ZERO;
        BigDecimal payable = booking.getPayableAmount() != null ? booking.getPayableAmount() : BigDecimal.ZERO;

        if (currentStatus == BookingStatus.WORKER_ON_THE_WAY || currentStatus == BookingStatus.ARRIVED) {
            // Notice window penalty: ₹50 or 10% fee if worker is already dispatched
            cancellationFee = BigDecimal.valueOf(50.00);
            if (cancellationFee.compareTo(payable) > 0) {
                cancellationFee = payable;
            }
        }

        BigDecimal refundAmount = payable.subtract(cancellationFee);
        if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            refundAmount = BigDecimal.ZERO;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason() + (request.getComments() != null ? " - " + request.getComments() : ""));
        booking.setCancellationFee(cancellationFee);
        booking.setRefundAmount(refundAmount);
        booking.setCancelledAt(LocalDateTime.now());

        bookingRepository.save(booking);

        return CancelBookingResponseDTO.builder()
                .bookingId(booking.getId())
                .status(BookingStatus.CANCELLED)
                .cancellationReason(booking.getCancellationReason())
                .cancellationFee(cancellationFee)
                .refundAmount(refundAmount)
                .cancelledAt(booking.getCancelledAt())
                .message("Booking cancelled successfully. Eligible refund: Rs " + refundAmount)
                .build();
    }

    @Override
    public BookingResponseDTO regenerateStartPin(Long bookingId, String customerIdHeader) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (customerIdHeader != null && !customerIdHeader.isBlank() && !booking.getCustomerId().equals(customerIdHeader.trim())) {
            throw new BookingNotFoundException("Booking not found with id: " + bookingId + " for customer: " + customerIdHeader);
        }

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Cannot regenerate PIN for " + booking.getStatus() + " booking");
        }

        String rawPin = String.format("%04d", 1000 + SECURE_RANDOM.nextInt(9000));
        String pinHash = passwordEncoder.encode(rawPin);

        booking.setStartPinHash(pinHash);
        booking.setStartPinEncrypted(rawPin);
        booking.setPinAttempts(0);
        booking.setPinExpiresAt(LocalDateTime.now().plusHours(4));

        Booking updatedBooking = bookingRepository.save(booking);
        return mapToResponseDTO(updatedBooking);
    }

    @Override
    public BookingResponseDTO updateBookingStatus(Long bookingId, BookingStatus status, String workerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        booking.setStatus(status);
        if (workerId != null && !workerId.isBlank()) {
            booking.setWorkerId(workerId);
        }

        if (status == BookingStatus.ACCEPTED || status == BookingStatus.ASSIGNED) {
            if (booking.getAcceptedAt() == null) {
                booking.setAcceptedAt(LocalDateTime.now());
            }
            if (booking.getStartPinEncrypted() == null) {
                String rawPin = String.format("%04d", 1000 + SECURE_RANDOM.nextInt(9000));
                booking.setStartPinHash(passwordEncoder.encode(rawPin));
                booking.setStartPinEncrypted(rawPin);
                booking.setPinAttempts(0);
                booking.setStartPinVerified(false);
                booking.setPinExpiresAt(LocalDateTime.now().plusDays(2));
            }
        } else if (status == BookingStatus.IN_PROGRESS && booking.getServiceStartedAt() == null) {
            booking.setServiceStartedAt(LocalDateTime.now());
        } else if (status == BookingStatus.COMPLETED && booking.getServiceCompletedAt() == null) {
            booking.setServiceCompletedAt(LocalDateTime.now());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return mapToResponseDTO(updatedBooking);
    }

    @Override
    public BookingResponseDTO verifyPin(Long bookingId, String pin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.ARRIVED && booking.getStatus() != BookingStatus.WORKER_ON_THE_WAY) {
            throw new InvalidBookingStateException("PIN can only be verified when worker has arrived or is at customer location");
        }

        if (booking.getPinAttempts() != null && booking.getPinAttempts() >= 5) {
            throw new InvalidPinException("Maximum PIN verification attempts exceeded. Please regenerate PIN.");
        }

        boolean isValid = false;
        if (booking.getStartPinHash() != null && passwordEncoder.matches(pin, booking.getStartPinHash())) {
            isValid = true;
        } else if (pin.equals(booking.getStartPinEncrypted())) {
            isValid = true;
        }

        if (!isValid) {
            booking.setPinAttempts((booking.getPinAttempts() != null ? booking.getPinAttempts() : 0) + 1);
            bookingRepository.save(booking);
            throw new InvalidPinException("Invalid start PIN. Attempts remaining: " + (5 - booking.getPinAttempts()));
        }

        booking.setStartPinVerified(true);
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setServiceStartedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        return mapToResponseDTO(saved);
    }

    @Override
    public WorkerActionResponseDTO startTravel(Long bookingId, String workerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getWorkerId() == null || !booking.getWorkerId().equals(workerId)) {
            throw new InvalidBookingStateException("Worker " + workerId + " is not assigned to booking #" + bookingId);
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED && booking.getStatus() != BookingStatus.ASSIGNED && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Cannot start travel for booking in status: " + booking.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        booking.setStatus(BookingStatus.EN_ROUTE);
        booking.setEnRouteAt(now);
        bookingRepository.save(booking);

        String navUrl = GeoDistanceUtils.buildGoogleMapsNavigationUrl(booking.getCustomerLat(), booking.getCustomerLng());

        return WorkerActionResponseDTO.builder()
                .bookingId(bookingId)
                .workerId(workerId)
                .status(BookingStatus.EN_ROUTE)
                .message("Travel started. Turn-by-turn navigation initiated to customer destination.")
                .navigationUrl(navUrl)
                .actionTimestamp(now)
                .build();
    }

    @Override
    public WorkerActionResponseDTO workerArrived(Long bookingId, String workerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getWorkerId() == null || !booking.getWorkerId().equals(workerId)) {
            throw new InvalidBookingStateException("Worker " + workerId + " is not assigned to booking #" + bookingId);
        }

        if (booking.getStatus() != BookingStatus.EN_ROUTE && booking.getStatus() != BookingStatus.WORKER_ON_THE_WAY && booking.getStatus() != BookingStatus.ACCEPTED && booking.getStatus() != BookingStatus.ASSIGNED) {
            throw new InvalidBookingStateException("Cannot mark ARRIVED for booking in status: " + booking.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        booking.setStatus(BookingStatus.ARRIVED);
        booking.setArrivedAt(now);
        bookingRepository.save(booking);

        return WorkerActionResponseDTO.builder()
                .bookingId(bookingId)
                .workerId(workerId)
                .status(BookingStatus.ARRIVED)
                .message("Arrived at customer location. Ask customer for 4-digit start PIN to start service.")
                .navigationUrl(null)
                .actionTimestamp(now)
                .build();
    }

    @Override
    public WorkerActionResponseDTO verifyStartPin(Long bookingId, String workerId, String pin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getWorkerId() == null || !booking.getWorkerId().equals(workerId)) {
            throw new InvalidBookingStateException("Worker " + workerId + " is not assigned to booking #" + bookingId);
        }

        if (booking.getStatus() != BookingStatus.ARRIVED) {
            throw new InvalidBookingStateException("PIN verification requires booking to be in ARRIVED status. Current: " + booking.getStatus());
        }

        if (Boolean.TRUE.equals(booking.getStartPinVerified())) {
            throw new InvalidBookingStateException("Start PIN has already been verified for this booking.");
        }

        if (booking.getPinAttempts() != null && booking.getPinAttempts() >= 3) {
            throw new InvalidPinException("Maximum 3 PIN verification attempts exceeded. Please regenerate PIN.");
        }

        boolean isValid = false;
        if (booking.getStartPinHash() != null && passwordEncoder.matches(pin, booking.getStartPinHash())) {
            isValid = true;
        } else if (pin != null && pin.equals(booking.getStartPinEncrypted())) {
            isValid = true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!isValid) {
            int attempts = (booking.getPinAttempts() != null ? booking.getPinAttempts() : 0) + 1;
            booking.setPinAttempts(attempts);
            bookingRepository.save(booking);
            int remaining = Math.max(0, 3 - attempts);
            throw new InvalidPinException("Incorrect 4-digit PIN. Attempts remaining: " + remaining);
        }

        booking.setStartPinVerified(true);
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setServiceStartedAt(now);
        bookingRepository.save(booking);

        return WorkerActionResponseDTO.builder()
                .bookingId(bookingId)
                .workerId(workerId)
                .status(BookingStatus.IN_PROGRESS)
                .message("Service start PIN verified successfully. Service is now IN_PROGRESS.")
                .navigationUrl(null)
                .actionTimestamp(now)
                .build();
    }

    @Override
    public WorkerActionResponseDTO completeService(Long bookingId, String workerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getWorkerId() == null || !booking.getWorkerId().equals(workerId)) {
            throw new InvalidBookingStateException("Worker " + workerId + " is not assigned to booking #" + bookingId);
        }

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new InvalidBookingStateException("Service cannot be completed unless it is IN_PROGRESS. Current: " + booking.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setServiceCompletedAt(now);
        bookingRepository.save(booking);

        // Increment worker completed jobs count
        try {
            UUID wUuid = UUID.fromString(workerId);
            workerProfileRepository.findById(wUuid).ifPresent(w -> {
                w.setCompletedJobsCount((w.getCompletedJobsCount() != null ? w.getCompletedJobsCount() : 0) + 1);
                workerProfileRepository.save(w);
            });
        } catch (IllegalArgumentException ignored) {
        }

        return WorkerActionResponseDTO.builder()
                .bookingId(bookingId)
                .workerId(workerId)
                .status(BookingStatus.COMPLETED)
                .message("Service completed successfully! Job recorded.")
                .actionTimestamp(now)
                .build();
    }

    @Override
    public WorkerLocationResponseDTO updateWorkerLocation(Long bookingId, String workerId, WorkerLocationRequestDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getWorkerId() == null || !booking.getWorkerId().equals(workerId)) {
            throw new InvalidBookingStateException("Worker " + workerId + " is not assigned to booking #" + bookingId);
        }

        com.salaryneeds.entity.WorkerLocation location = com.salaryneeds.entity.WorkerLocation.builder()
                .bookingId(bookingId)
                .workerId(workerId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracy(request.getAccuracy())
                .build();

        com.salaryneeds.entity.WorkerLocation saved = workerLocationRepository.save(location);

        // Also update worker profile last known location
        try {
            UUID workerUuid = UUID.fromString(workerId);
            workerProfileRepository.findById(workerUuid).ifPresent(worker -> {
                worker.setLastLat(request.getLatitude());
                worker.setLastLng(request.getLongitude());
                workerProfileRepository.save(worker);
            });
        } catch (IllegalArgumentException ignored) {
        }

        WorkerLocationResponseDTO responseDTO = WorkerLocationResponseDTO.builder()
                .bookingId(bookingId)
                .workerId(workerId)
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .accuracy(saved.getAccuracy())
                .timestamp(saved.getTimestamp() != null ? saved.getTimestamp() : LocalDateTime.now())
                .build();

        // Broadcast to WebSocket: /topic/bookings/{bookingId}/worker-location
        notificationService.broadcastWorkerLocation(bookingId, responseDTO);

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkerLocationResponseDTO getLatestWorkerLocation(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        return workerLocationRepository.findFirstByBookingIdOrderByTimestampDesc(bookingId)
                .map(loc -> WorkerLocationResponseDTO.builder()
                        .bookingId(loc.getBookingId())
                        .workerId(loc.getWorkerId())
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .accuracy(loc.getAccuracy())
                        .timestamp(loc.getTimestamp())
                        .build())
                .orElseGet(() -> {
                    // Fallback to worker profile lastLat/lastLng if trip just started
                    if (booking.getWorkerId() != null) {
                        try {
                            UUID wUuid = UUID.fromString(booking.getWorkerId());
                            return workerProfileRepository.findById(wUuid)
                                    .filter(w -> w.getLastLat() != null && w.getLastLng() != null)
                                    .map(w -> WorkerLocationResponseDTO.builder()
                                            .bookingId(bookingId)
                                            .workerId(booking.getWorkerId())
                                            .latitude(w.getLastLat())
                                            .longitude(w.getLastLng())
                                            .accuracy(null)
                                            .timestamp(LocalDateTime.now())
                                            .build())
                                    .orElse(null);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    return null;
                });
    }

    private BookingResponseDTO mapToResponseDTO(Booking booking) {
        // 4-digit start PIN is revealed to the customer once a worker accepts the booking
        boolean isAcceptedOrLater = booking.getStatus() == BookingStatus.ACCEPTED
                || booking.getStatus() == BookingStatus.ASSIGNED
                || booking.getStatus() == BookingStatus.EN_ROUTE
                || booking.getStatus() == BookingStatus.WORKER_ON_THE_WAY
                || booking.getStatus() == BookingStatus.ARRIVED
                || booking.getStatus() == BookingStatus.IN_PROGRESS;
        String revealedPin = isAcceptedOrLater ? booking.getStartPinEncrypted() : null;
        String navUrl = GeoDistanceUtils.buildGoogleMapsNavigationUrl(booking.getCustomerLat(), booking.getCustomerLng());

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .customerId(booking.getCustomerId())
                .workerId(booking.getWorkerId())
                .serviceId(booking.getServiceId())
                .serviceName(booking.getServiceName())
                .categoryId(booking.getCategoryId())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .payableAmount(booking.getPayableAmount())
                .addressId(booking.getAddressId())
                .addressSummary(booking.getAddressSummary())
                .customerLat(booking.getCustomerLat())
                .customerLng(booking.getCustomerLng())
                .navigationUrl(navUrl)
                .slotId(booking.getSlotId())
                .scheduledTime(booking.getScheduledTime())
                .couponCode(booking.getCouponCode())
                .notes(booking.getNotes())
                .startPin(revealedPin)
                .startPinVerified(booking.getStartPinVerified())
                .pinExpiresAt(booking.getPinExpiresAt())
                .acceptedAt(booking.getAcceptedAt())
                .enRouteAt(booking.getEnRouteAt())
                .arrivedAt(booking.getArrivedAt())
                .cancellationReason(booking.getCancellationReason())
                .cancellationFee(booking.getCancellationFee())
                .refundAmount(booking.getRefundAmount())
                .cancelledAt(booking.getCancelledAt())
                .serviceStartedAt(booking.getServiceStartedAt())
                .serviceCompletedAt(booking.getServiceCompletedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private record SlotDefinition(String slotId, String timeRange, LocalTime startTime, LocalTime endTime) {}
}
