package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerActionResponseDTO;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.BookingOffer;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.BookingOfferStatus;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.exception.BookingAlreadyAssignedException;
import com.salaryneeds.exception.BookingOfferNotFoundException;
import com.salaryneeds.exception.InvalidBookingStateException;
import com.salaryneeds.exception.WorkerNotFoundException;
import com.salaryneeds.repository.BookingOfferRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.util.GeoDistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingOfferServiceImpl implements BookingOfferService {

    private final BookingOfferRepository bookingOfferRepository;
    private final BookingRepository bookingRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.ACCEPTED,
            BookingStatus.ASSIGNED,
            BookingStatus.EN_ROUTE,
            BookingStatus.WORKER_ON_THE_WAY,
            BookingStatus.ARRIVED,
            BookingStatus.IN_PROGRESS
    );

    private static final List<BookingStatus> ASSIGNABLE_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.MATCHING,
            BookingStatus.OFFERED
    );

    @Override
    @Transactional(readOnly = true)
    public List<BookingOfferResponseDTO> getOffersForWorker(String workerId, BookingOfferStatus status) {
        BookingOfferStatus effectiveStatus = (status != null) ? status : BookingOfferStatus.PENDING;
        List<BookingOffer> offers = bookingOfferRepository.findByWorkerIdAndStatusOrderByCreatedAtDesc(workerId, effectiveStatus);

        return offers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkerActionResponseDTO acceptOffer(Long offerId, String workerId) {
        log.info("Worker {} attempting to accept offer #{}", workerId, offerId);

        // 1. Fetch and validate offer
        BookingOffer offer = bookingOfferRepository.findByIdAndWorkerId(offerId, workerId)
                .orElseThrow(() -> new BookingOfferNotFoundException("Offer not found or unauthorized for worker"));

        if (offer.getStatus() != BookingOfferStatus.PENDING) {
            throw new InvalidBookingStateException("Offer is no longer pending. Current status: " + offer.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if (offer.getExpiresAt() != null && now.isAfter(offer.getExpiresAt())) {
            offer.setStatus(BookingOfferStatus.EXPIRED);
            offer.setRespondedAt(now);
            bookingOfferRepository.save(offer);
            throw new InvalidBookingStateException("Booking offer has expired");
        }

        // 2. Validate Worker approval & availability
        try {
            UUID workerUuid = UUID.fromString(workerId);
            WorkerProfile worker = workerProfileRepository.findById(workerUuid)
                    .orElseThrow(() -> new WorkerNotFoundException("Worker profile not found"));

            if (!Boolean.TRUE.equals(worker.getVerified())) {
                throw new InvalidBookingStateException("Worker profile is not yet approved/verified");
            }
            if (!Boolean.TRUE.equals(worker.getDutyOnline()) || worker.getAccountStatus() != com.salaryneeds.entity.enums.AccountStatus.ACTIVE) {
                throw new InvalidBookingStateException("Worker is currently offline or inactive");
            }
        } catch (IllegalArgumentException ignored) {
        }

        // 3. Ensure worker has no conflicting active bookings
        boolean hasActiveBooking = bookingRepository.existsByWorkerIdAndStatusIn(workerId, ACTIVE_BOOKING_STATUSES);
        if (hasActiveBooking) {
            throw new InvalidBookingStateException("Worker is already busy with an active booking");
        }

        // 4. ATOMIC CONDITIONAL UPDATE:
        // Ensures exactly ONE worker can successfully claim and accept the booking
        Booking booking = offer.getBooking();
        int rowsUpdated = bookingRepository.assignWorkerAtomically(
                booking.getId(),
                workerId,
                BookingStatus.ACCEPTED,
                ASSIGNABLE_STATUSES,
                now
        );

        if (rowsUpdated == 0) {
            // Another worker won the race condition!
            offer.setStatus(BookingOfferStatus.CANCELLED);
            offer.setRespondedAt(now);
            bookingOfferRepository.save(offer);
            log.warn("Worker {} lost race to accept booking #{}. Marked offer as CANCELLED.", workerId, booking.getId());
            throw new BookingAlreadyAssignedException("This booking has already been accepted by another worker");
        }

        // 4b. Generate 4-digit start PIN on worker acceptance
        String rawPin = String.format("%04d", 1000 + SECURE_RANDOM.nextInt(9000));
        String pinHash = (passwordEncoder != null) ? passwordEncoder.encode(rawPin) : rawPin;
        LocalDateTime pinExpiresAt = now.plusDays(2);

        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setWorkerId(workerId);
        booking.setAcceptedAt(now);
        booking.setStartPinHash(pinHash);
        booking.setStartPinEncrypted(rawPin);
        booking.setPinAttempts(0);
        booking.setStartPinVerified(false);
        booking.setPinExpiresAt(pinExpiresAt);
        bookingRepository.save(booking);

        // 5. Mark winning offer as ACCEPTED
        offer.setStatus(BookingOfferStatus.ACCEPTED);
        offer.setRespondedAt(now);
        bookingOfferRepository.save(offer);

        // 6. Automatically cancel competing pending offers for this booking
        bookingOfferRepository.cancelCompetingOffers(booking.getId(), offer.getId(), BookingOfferStatus.CANCELLED, now);
        log.info("Booking #{} successfully assigned to worker {}. 4-digit start PIN generated. Competing offers cancelled.", booking.getId(), workerId);

        // 7. Generate Navigation URL to customer
        String navUrl = GeoDistanceUtils.buildGoogleMapsNavigationUrl(booking.getCustomerLat(), booking.getCustomerLng());

        return WorkerActionResponseDTO.builder()
                .bookingId(booking.getId())
                .workerId(workerId)
                .status(BookingStatus.ACCEPTED)
                .message("Booking offer accepted successfully! You are now assigned to this service.")
                .navigationUrl(navUrl)
                .actionTimestamp(now)
                .build();
    }

    @Override
    @Transactional
    public WorkerActionResponseDTO rejectOffer(Long offerId, String workerId) {
        BookingOffer offer = bookingOfferRepository.findByIdAndWorkerId(offerId, workerId)
                .orElseThrow(() -> new BookingOfferNotFoundException("Offer not found or unauthorized for worker"));

        if (offer.getStatus() != BookingOfferStatus.PENDING) {
            throw new InvalidBookingStateException("Offer is no longer pending. Current status: " + offer.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        offer.setStatus(BookingOfferStatus.REJECTED);
        offer.setRespondedAt(now);
        bookingOfferRepository.save(offer);
        log.info("Worker {} rejected offer #{} for booking #{}", workerId, offerId, offer.getBooking().getId());

        return WorkerActionResponseDTO.builder()
                .bookingId(offer.getBooking().getId())
                .workerId(workerId)
                .status(BookingStatus.OFFERED)
                .message("Booking offer rejected")
                .actionTimestamp(now)
                .build();
    }

    private BookingOfferResponseDTO mapToDTO(BookingOffer offer) {
        Booking booking = offer.getBooking();
        return BookingOfferResponseDTO.builder()
                .id(offer.getId())
                .bookingId(booking.getId())
                .workerId(offer.getWorkerId())
                .serviceName(booking.getServiceName())
                .categoryId(booking.getCategoryId())
                .customerArea(booking.getAddressSummary())
                .distanceKm(offer.getDistanceKm())
                .totalAmount(booking.getTotalAmount())
                .payableAmount(booking.getPayableAmount())
                .bookingDate(booking.getBookingDate())
                .scheduledTime(booking.getScheduledTime())
                .slotId(booking.getSlotId())
                .status(offer.getStatus())
                .offeredAt(offer.getOfferedAt())
                .expiresAt(offer.getExpiresAt())
                .respondedAt(offer.getRespondedAt())
                .build();
    }
}
