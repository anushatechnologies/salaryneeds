package com.salaryneeds;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerActionResponseDTO;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.BookingOffer;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.AccountStatus;
import com.salaryneeds.entity.enums.BookingOfferStatus;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.exception.BookingAlreadyAssignedException;
import com.salaryneeds.repository.BookingOfferRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.service.*;
import com.salaryneeds.util.GeoDistanceUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerMatchingAndExecutionTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingOfferRepository bookingOfferRepository;

    @Mock
    private WorkerProfileRepository workerProfileRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private WorkerMatchingServiceImpl workerMatchingService;

    @InjectMocks
    private BookingOfferServiceImpl bookingOfferService;

    private static final String GOOGLE_MAPS_KEY = "AIzaSyAJfaQLhzeEpekoZb8SJwYkoogodbXbr-o";

    @Test
    @DisplayName("GeoDistanceUtils: Haversine distance calculates accurately and respects radius")
    void testGeoDistanceCalculation() {
        // HSR Layout Bangalore (12.9121, 77.6446) to Koramangala Bangalore (12.9352, 77.6245) ~ 3.3 km
        Double dist = GeoDistanceUtils.calculateDistanceKm(12.9121, 77.6446, 12.9352, 77.6245);
        assertNotNull(dist);
        assertTrue(dist > 2.5 && dist < 4.0, "Distance should be ~3.3km");

        // Null coordinate handling
        assertNull(GeoDistanceUtils.calculateDistanceKm(null, 77.6446, 12.9352, 77.6245));
    }

    @Test
    @DisplayName("GeoDistanceUtils: Google Maps URLs generated properly with navigation mode and API key")
    void testGoogleMapsUrls() {
        String navUrl = GeoDistanceUtils.buildGoogleMapsNavigationUrl(12.9121, 77.6446);
        assertNotNull(navUrl);
        assertTrue(navUrl.contains("www.google.com/maps/dir/?api=1"));
        assertTrue(navUrl.contains("destination=12.912100,77.644600"));
        assertTrue(navUrl.contains("travelmode=driving"));

        String staticMapUrl = GeoDistanceUtils.buildStaticMapPreviewUrl(12.9121, 77.6446, GOOGLE_MAPS_KEY);
        assertNotNull(staticMapUrl);
        assertTrue(staticMapUrl.contains("maps.googleapis.com/maps/api/staticmap"));
        assertTrue(staticMapUrl.contains("key=" + GOOGLE_MAPS_KEY));

        String embedMapUrl = GeoDistanceUtils.buildEmbedMapUrl(12.9121, 77.6446, GOOGLE_MAPS_KEY);
        assertNotNull(embedMapUrl);
        assertTrue(embedMapUrl.contains("www.google.com/maps/embed"));
        assertTrue(embedMapUrl.contains("key=" + GOOGLE_MAPS_KEY));
    }

    @Test
    @DisplayName("Matching Engine: Finds only workers within configured service radius")
    void testMatchingFiltersByServiceRadius() {
        // Customer at HSR Layout (12.9121, 77.6446)
        Booking booking = Booking.builder()
                .id(200L)
                .serviceId(1L)
                .serviceName("AC Repair")
                .categoryId(null)
                .customerLat(12.9121)
                .customerLng(77.6446)
                .payableAmount(new BigDecimal("499.00"))
                .bookingDate(LocalDate.now())
                .scheduledTime("10:00 AM")
                .status(BookingStatus.MATCHING)
                .build();

        // Worker 1: Koramangala (3.3 km away), radius 10 km -> ELIGIBLE
        UUID worker1Id = UUID.randomUUID();
        WorkerProfile worker1 = WorkerProfile.builder()
                .id(worker1Id)
                .name("Ravi Kumar")
                .phone("+919876543210")
                .verified(true)
                .dutyOnline(true)
                .serviceAreaLat(12.9352)
                .serviceAreaLng(77.6245)
                .serviceRadiusKm(10.0)
                .build();

        // Worker 2: Whitefield (~18 km away), radius 5 km -> OUT OF RADIUS
        UUID worker2Id = UUID.randomUUID();
        WorkerProfile worker2 = WorkerProfile.builder()
                .id(worker2Id)
                .name("Suresh Patel")
                .phone("+919876543211")
                .verified(true)
                .dutyOnline(true)
                .serviceAreaLat(12.9698)
                .serviceAreaLng(77.7499)
                .serviceRadiusKm(5.0)
                .build();

        when(workerProfileRepository.findEligibleCandidateWorkers(isNull(), eq("AC Repair")))
                .thenReturn(List.of(worker1, worker2));
        when(bookingRepository.existsByWorkerIdAndStatusIn(anyString(), anyList()))
                .thenReturn(false);
        when(bookingOfferRepository.save(any(BookingOffer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<BookingOfferResponseDTO> offers = workerMatchingService.matchAndCreateOffers(booking);

        // Only Worker 1 should receive offer
        assertEquals(1, offers.size());
        assertEquals(worker1Id.toString(), offers.get(0).getWorkerId());
        assertTrue(offers.get(0).getDistanceKm() < 10.0);
        verify(notificationService, times(1)).notifyWorkerOfOffer(anyString(), any(BookingOfferResponseDTO.class));
    }

    @Test
    @DisplayName("Offer Acceptance: Safe atomic assignment and competing offers cancellation")
    void testAtomicOfferAcceptance() {
        UUID workerUuid = UUID.randomUUID();
        String workerId = workerUuid.toString();
        Booking booking = Booking.builder()
                .id(300L)
                .customerLat(12.9121)
                .customerLng(77.6446)
                .status(BookingStatus.OFFERED)
                .build();

        BookingOffer offer = BookingOffer.builder()
                .id(1L)
                .booking(booking)
                .workerId(workerId)
                .status(BookingOfferStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        WorkerProfile profile = WorkerProfile.builder()
                .id(workerUuid)
                .name("Ravi Kumar")
                .verified(true)
                .dutyOnline(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(bookingOfferRepository.findByIdAndWorkerId(1L, workerId)).thenReturn(Optional.of(offer));
        when(workerProfileRepository.findById(workerUuid)).thenReturn(Optional.of(profile));
        when(bookingRepository.existsByWorkerIdAndStatusIn(eq(workerId), anyList())).thenReturn(false);
        when(bookingRepository.assignWorkerAtomically(eq(300L), eq(workerId), any(BookingStatus.class), anyCollection(), any(LocalDateTime.class))).thenReturn(1);
        when(bookingOfferRepository.save(any(BookingOffer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hashedPin");

        WorkerActionResponseDTO response = bookingOfferService.acceptOffer(1L, workerId);

        assertNotNull(response);
        assertEquals(BookingStatus.ACCEPTED, response.getStatus());
        assertEquals(workerId, response.getWorkerId());
        assertNotNull(response.getNavigationUrl());
        assertNotNull(booking.getStartPinEncrypted(), "4-digit PIN should be generated upon worker offer acceptance");
        assertEquals(4, booking.getStartPinEncrypted().length());
        assertEquals("hashedPin", booking.getStartPinHash());

        // Verify competing offers cancelled
        verify(bookingOfferRepository, times(1)).cancelCompetingOffers(eq(300L), eq(1L), any(BookingOfferStatus.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Offer Acceptance: Rejection if another worker already accepted (concurrency conflict)")
    void testOfferAcceptanceConflict() {
        UUID workerUuid = UUID.randomUUID();
        String workerId = workerUuid.toString();
        Booking booking = Booking.builder()
                .id(301L)
                .build();

        BookingOffer offer = BookingOffer.builder()
                .id(2L)
                .booking(booking)
                .workerId(workerId)
                .status(BookingOfferStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        WorkerProfile profile = WorkerProfile.builder()
                .id(workerUuid)
                .name("Ravi Kumar")
                .verified(true)
                .dutyOnline(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(bookingOfferRepository.findByIdAndWorkerId(2L, workerId)).thenReturn(Optional.of(offer));
        when(workerProfileRepository.findById(workerUuid)).thenReturn(Optional.of(profile));
        when(bookingRepository.existsByWorkerIdAndStatusIn(eq(workerId), anyList())).thenReturn(false);
        // Atomic DB update returns 0 -> another worker got it first
        when(bookingRepository.assignWorkerAtomically(eq(301L), eq(workerId), any(BookingStatus.class), anyCollection(), any(LocalDateTime.class))).thenReturn(0);

        assertThrows(BookingAlreadyAssignedException.class, () -> {
            bookingOfferService.acceptOffer(2L, workerId);
        });

        assertEquals(BookingOfferStatus.CANCELLED, offer.getStatus());
    }
}
