package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Address;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.entity.enums.BookingStatusTab;
import com.salaryneeds.exception.*;
import com.salaryneeds.repository.AddressRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.ServiceItemRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final CouponService couponService;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponseDTO> getAvailableSlots(Long serviceId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<SlotDefinition> definitions = Arrays.asList(
                new SlotDefinition("SLOT-0912", "09:00 AM - 12:00 PM", LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new SlotDefinition("SLOT-1215", "12:00 PM - 03:00 PM", LocalTime.of(12, 0), LocalTime.of(15, 0)),
                new SlotDefinition("SLOT-1518", "03:00 PM - 06:00 PM", LocalTime.of(15, 0), LocalTime.of(18, 0)),
                new SlotDefinition("SLOT-1821", "06:00 PM - 09:00 PM", LocalTime.of(18, 0), LocalTime.of(21, 0))
        );

        List<SlotResponseDTO> slots = new ArrayList<>();

        for (SlotDefinition def : definitions) {
            LocalDateTime slotStart = targetDate.atTime(def.startTime);
            LocalDateTime cutoffTime = slotStart.minusHours(1); // 1 hour cutoff prior to slot start

            boolean isAvailable = true;
            String message = "Available";

            if (targetDate.isBefore(LocalDate.now())) {
                isAvailable = false;
                message = "Slot date is in the past";
            } else if (targetDate.isEqual(LocalDate.now()) && now.isAfter(cutoffTime)) {
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

        // Address Summary
        String addressSummary = null;
        if (request.getAddressId() != null && !request.getAddressId().isBlank()) {
            try {
                UUID addrUuid = UUID.fromString(request.getAddressId());
                Optional<Address> addrOpt = addressRepository.findById(addrUuid);
                if (addrOpt.isPresent()) {
                    addressSummary = addrOpt.get().toFormattedAddress();
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

        // Generate 4-digit start PIN
        String rawPin = String.format("%04d", 1000 + SECURE_RANDOM.nextInt(9000));
        String pinHash = passwordEncoder.encode(rawPin);
        LocalDateTime pinExpiresAt = request.getBookingDate().atTime(23, 59, 59);

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
                .slotId(request.getSlotId() != null ? request.getSlotId() : "SLOT-0912")
                .scheduledTime(request.getScheduledTime() != null ? request.getScheduledTime() : "09:00 AM - 12:00 PM")
                .couponCode(request.getCouponCode())
                .notes(request.getNotes())
                .startPinHash(pinHash)
                .startPinEncrypted(rawPin) // Secure storage for ARRIVED reveal
                .startPinVerified(false)
                .pinAttempts(0)
                .pinExpiresAt(pinExpiresAt)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
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

        if (status == BookingStatus.IN_PROGRESS && booking.getServiceStartedAt() == null) {
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

    private BookingResponseDTO mapToResponseDTO(Booking booking) {
        // SECURITY RULE: 4-digit start PIN is ONLY revealed to the customer when status == ARRIVED
        String revealedPin = (booking.getStatus() == BookingStatus.ARRIVED) ? booking.getStartPinEncrypted() : null;

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
                .slotId(booking.getSlotId())
                .scheduledTime(booking.getScheduledTime())
                .couponCode(booking.getCouponCode())
                .notes(booking.getNotes())
                .startPin(revealedPin)
                .startPinVerified(booking.getStartPinVerified())
                .pinExpiresAt(booking.getPinExpiresAt())
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
