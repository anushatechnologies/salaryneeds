package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.BookingOffer;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.BookingOfferStatus;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.repository.BookingOfferRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.util.GeoDistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerMatchingServiceImpl implements WorkerMatchingService {

    private final WorkerProfileRepository workerProfileRepository;
    private final BookingOfferRepository bookingOfferRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.ACCEPTED,
            BookingStatus.ASSIGNED,
            BookingStatus.EN_ROUTE,
            BookingStatus.WORKER_ON_THE_WAY,
            BookingStatus.ARRIVED,
            BookingStatus.IN_PROGRESS
    );

    private static final double DEFAULT_SERVICE_RADIUS_KM = 15.0;
    private static final int OFFER_VALIDITY_MINUTES = 15;

    @Override
    @Transactional
    public List<BookingOfferResponseDTO> matchAndCreateOffers(Booking booking) {
        if (booking == null || booking.getId() == null) {
            log.warn("Cannot match workers for null or unsaved booking");
            return Collections.emptyList();
        }

        Double customerLat = booking.getCustomerLat();
        Double customerLng = booking.getCustomerLng();

        // 1. Resolve Category UUID if present
        UUID categoryUuid = null;
        if (booking.getCategoryId() != null && !booking.getCategoryId().isBlank()) {
            try {
                categoryUuid = UUID.fromString(booking.getCategoryId().trim());
            } catch (IllegalArgumentException ignored) {
            }
        }

        // 2. Fetch eligible candidate workers (approved, online, matching category or service name)
        List<WorkerProfile> candidates = workerProfileRepository.findEligibleCandidateWorkers(categoryUuid, booking.getServiceName());
        log.info("Found {} candidate workers for booking #{}", candidates.size(), booking.getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OFFER_VALIDITY_MINUTES);

        List<BookingOffer> createdOffers = new ArrayList<>();

        for (WorkerProfile worker : candidates) {
            String workerId = worker.getId().toString();

            // 3. Prevent duplicate offers for the same booking
            if (bookingOfferRepository.existsByBookingIdAndWorkerId(booking.getId(), workerId)) {
                log.debug("Worker {} already has an offer for booking #{}, skipping", workerId, booking.getId());
                continue;
            }

            // 4. Ensure worker is not already assigned to an active conflicting booking
            boolean hasActiveBooking = bookingRepository.existsByWorkerIdAndStatusIn(workerId, ACTIVE_BOOKING_STATUSES);
            if (hasActiveBooking) {
                log.debug("Worker {} is currently busy with an active booking, skipping", workerId);
                continue;
            }

            // 5. Geographic Proximity Check:
            // Worker's configured service area coordinates (fallback to live GPS coordinates if not configured)
            Double workerBaseLat = worker.getServiceAreaLat() != null ? worker.getServiceAreaLat() : worker.getLastLat();
            Double workerBaseLng = worker.getServiceAreaLng() != null ? worker.getServiceAreaLng() : worker.getLastLng();
            double allowedRadiusKm = (worker.getServiceRadiusKm() != null && worker.getServiceRadiusKm() > 0)
                    ? worker.getServiceRadiusKm()
                    : DEFAULT_SERVICE_RADIUS_KM;

            Double distanceKm = null;
            if (customerLat != null && customerLng != null && workerBaseLat != null && workerBaseLng != null) {
                distanceKm = GeoDistanceUtils.calculateDistanceKm(workerBaseLat, workerBaseLng, customerLat, customerLng);
                if (distanceKm != null && distanceKm > allowedRadiusKm) {
                    log.debug("Worker {} is at distance {} km which exceeds configured radius {} km, skipping",
                            workerId, distanceKm, allowedRadiusKm);
                    continue;
                }
            }

            // 6. Worker is eligible -> Create Booking Offer
            BookingOffer offer = BookingOffer.builder()
                    .booking(booking)
                    .workerId(workerId)
                    .distanceKm(distanceKm)
                    .status(BookingOfferStatus.PENDING)
                    .offeredAt(now)
                    .expiresAt(expiresAt)
                    .build();

            BookingOffer savedOffer = bookingOfferRepository.save(offer);
            createdOffers.add(savedOffer);

            // 7. Real-time Dispatch
            BookingOfferResponseDTO offerDTO = mapToOfferDTO(savedOffer, booking);
            notificationService.notifyWorkerOfOffer(workerId, offerDTO);
        }

        // Update booking status to OFFERED if offers were generated
        if (!createdOffers.isEmpty() && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.OFFERED);
            bookingRepository.save(booking);
        }

        // Return sorted by proximity
        return createdOffers.stream()
                .map(offer -> mapToOfferDTO(offer, booking))
                .sorted(Comparator.comparing(BookingOfferResponseDTO::getDistanceKm, Comparator.nullsLast(Double::compareTo)))
                .collect(Collectors.toList());
    }

    private BookingOfferResponseDTO mapToOfferDTO(BookingOffer offer, Booking booking) {
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
