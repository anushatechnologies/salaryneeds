package com.salaryneeds;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Address;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.WorkerLocation;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.repository.AddressRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.WorkerLocationRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.service.BookingServiceImpl;
import com.salaryneeds.service.CallBridgeServiceImpl;
import com.salaryneeds.service.NotificationService;
import com.salaryneeds.service.WorkerMatchingService;
import com.salaryneeds.util.GeoDistanceUtils;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingFullLifecycleApiTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private WorkerProfileRepository workerProfileRepository;

    @Mock
    private WorkerLocationRepository workerLocationRepository;

    @Mock
    private com.salaryneeds.repository.ServiceItemRepository serviceItemRepository;

    @Mock
    private com.salaryneeds.service.CouponService couponService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WorkerMatchingService workerMatchingService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @InjectMocks
    private CallBridgeServiceImpl callBridgeService;

    private String customerId;
    private UUID workerUuid;
    private WorkerProfile sampleWorker;
    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID().toString();
        workerUuid = UUID.randomUUID();

        sampleWorker = WorkerProfile.builder()
                .id(workerUuid)
                .name("Ravi Kumar")
                .email("ravi.electrician@salaryneeds.com")
                .phone("+919876543210")
                .service("Electrician")
                .skills("Wiring, Circuit Breakers, Switchboards")
                .completedJobsCount(127)
                .experienceYears(5)
                .verified(true)
                .dutyOnline(true)
                .lastLat(17.4482)
                .lastLng(78.3914)
                .avatarUrl("https://images.unsplash.com/photo-1540569014015-19a7be504e3a")
                .build();
        sampleWorker.setRatingAvg(BigDecimal.valueOf(4.85));

        sampleBooking = Booking.builder()
                .id(1001L)
                .customerId(customerId)
                .workerId(workerUuid.toString())
                .serviceId(10L)
                .serviceName("Electrician Service")
                .bookingDate(LocalDate.now())
                .status(BookingStatus.ACCEPTED)
                .totalAmount(BigDecimal.valueOf(450.00))
                .discountAmount(BigDecimal.ZERO)
                .payableAmount(BigDecimal.valueOf(450.00))
                .customerLat(17.4933)
                .customerLng(78.3995)
                .addressSummary("Flat 304, Green Heights, Kukatpally, Hyderabad")
                .slotId("SLOT-0912")
                .scheduledTime("09:00 AM - 12:00 PM")
                .startPinEncrypted("4827")
                .startPinHash("$2a$10$abcdef1234567890")
                .startPinVerified(false)
                .pinAttempts(0)
                .build();
    }

    @Test
    @DisplayName("1. Test Available Slots API")
    void testGetAvailableSlots() {
        List<SlotResponseDTO> slots = bookingService.getAvailableSlots(10L, LocalDate.now());
        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        assertEquals(4, slots.size());
        assertEquals("SLOT-0912", slots.get(0).getSlotId());
    }

    @Test
    @DisplayName("2. Test Create Booking API - Enriches service and triggers matching")
    void testCreateBooking() {
        BookingCreateRequestDTO request = BookingCreateRequestDTO.builder()
                .customerId(customerId)
                .serviceId(10L)
                .serviceName("Electrician Service")
                .bookingDate(LocalDate.now().plusDays(1))
                .totalAmount(BigDecimal.valueOf(450.00))
                .slotId("SLOT-0912")
                .scheduledTime("09:00 AM - 12:00 PM")
                .notes("Fan sparking in living room")
                .customerLat(17.4933)
                .customerLng(78.3995)
                .build();

        when(customerRepository.existsById(any(UUID.class))).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(2001L);
            return b;
        });

        BookingResponseDTO response = bookingService.createBooking(request, customerId);

        assertNotNull(response);
        assertEquals(2001L, response.getId());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(workerMatchingService, times(1)).matchAndCreateOffers(any(Booking.class));
    }

    @Test
    @DisplayName("3. Test Worker Start Travel API - Dispatches live ETA and notification")
    void testStartTravel() {
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));
        when(workerProfileRepository.findById(workerUuid)).thenReturn(Optional.of(sampleWorker));

        WorkerActionResponseDTO response = bookingService.startTravel(1001L, workerUuid.toString());

        assertNotNull(response);
        assertEquals(BookingStatus.EN_ROUTE, response.getStatus());
        assertEquals(BookingStatus.EN_ROUTE, sampleBooking.getStatus());
        assertNotNull(sampleBooking.getEnRouteAt());

        verify(notificationService, times(1)).notifyCustomerBookingEvent(
                eq(customerId), eq(1001L), eq(BookingStatus.EN_ROUTE), anyString(), anyString(), anyMap()
        );
    }

    @Test
    @DisplayName("4. Test Worker Live GPS Stream & Dynamic ETA Calculation")
    void testWorkerLocationUpdateWithDynamicEta() {
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));
        when(workerLocationRepository.save(any(WorkerLocation.class))).thenAnswer(i -> i.getArgument(0));

        WorkerLocationRequestDTO locReq = WorkerLocationRequestDTO.builder()
                .latitude(17.4700)
                .longitude(78.3950)
                .accuracy(5.0)
                .build();

        WorkerLocationResponseDTO locRes = bookingService.updateWorkerLocation(1001L, workerUuid.toString(), locReq);

        assertNotNull(locRes);
        assertNotNull(locRes.getDistanceKm());
        assertTrue(locRes.getDistanceKm() > 0);
        assertNotNull(locRes.getEtaMinutes());
        assertTrue(locRes.getEtaMinutes() > 0);
        assertNotNull(locRes.getEtaText());

        verify(notificationService, times(1)).broadcastWorkerLocation(eq(1001L), any(WorkerLocationResponseDTO.class));
    }

    @Test
    @DisplayName("5. Test Masked / Privacy Calling Bridge API")
    void testMaskedCallBridge() {
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));

        InitiateCallRequestDTO callReq = new InitiateCallRequestDTO("DIRECTIONS");
        CallResponseDTO callRes = callBridgeService.initiateMaskedCall(1001L, customerId, null, callReq);

        assertNotNull(callRes);
        assertEquals("CONNECTING", callRes.getStatus());
        assertEquals("CUSTOMER", callRes.getCallerType());
        assertNotNull(callRes.getMaskedDisplayNumber());
        assertNotNull(callRes.getCallSessionId());
    }

    @Test
    @DisplayName("6. Test Worker Arrived API - Dispatches start PIN alert to customer")
    void testWorkerArrived() {
        sampleBooking.setStatus(BookingStatus.EN_ROUTE);
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));

        WorkerActionResponseDTO response = bookingService.workerArrived(1001L, workerUuid.toString());

        assertNotNull(response);
        assertEquals(BookingStatus.ARRIVED, response.getStatus());
        assertEquals(BookingStatus.ARRIVED, sampleBooking.getStatus());
        assertNotNull(sampleBooking.getArrivedAt());

        verify(notificationService, times(1)).notifyCustomerBookingEvent(
                eq(customerId), eq(1001L), eq(BookingStatus.ARRIVED), eq("Worker has arrived 📍"), anyString(), anyMap()
        );
    }

    @Test
    @DisplayName("7. Test Start PIN Verification API - Successfully transitions to IN_PROGRESS")
    void testVerifyStartPin_Success() {
        sampleBooking.setStatus(BookingStatus.ARRIVED);
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));
        when(passwordEncoder.matches("4827", "$2a$10$abcdef1234567890")).thenReturn(true);

        WorkerActionResponseDTO response = bookingService.verifyStartPin(1001L, workerUuid.toString(), "4827");

        assertNotNull(response);
        assertEquals(BookingStatus.IN_PROGRESS, response.getStatus());
        assertTrue(sampleBooking.getStartPinVerified());
        assertNotNull(sampleBooking.getServiceStartedAt());

        verify(notificationService, times(1)).notifyCustomerBookingEvent(
                eq(customerId), eq(1001L), eq(BookingStatus.IN_PROGRESS), eq("Service Started 🔧"), anyString(), anyMap()
        );
    }

    @Test
    @DisplayName("8. Test Complete Service API - Increments worker job count and notifies customer")
    void testCompleteService_Success() {
        sampleBooking.setStatus(BookingStatus.IN_PROGRESS);
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));
        when(workerProfileRepository.findById(workerUuid)).thenReturn(Optional.of(sampleWorker));

        WorkerActionResponseDTO response = bookingService.completeService(1001L, workerUuid.toString());

        assertNotNull(response);
        assertEquals(BookingStatus.COMPLETED, response.getStatus());
        assertEquals(BookingStatus.COMPLETED, sampleBooking.getStatus());
        assertNotNull(sampleBooking.getServiceCompletedAt());
        assertEquals(128, sampleWorker.getCompletedJobsCount()); // Incremented from 127 to 128

        verify(notificationService, times(1)).notifyCustomerBookingEvent(
                eq(customerId), eq(1001L), eq(BookingStatus.COMPLETED), eq("Service Completed 🎉"), anyString(), anyMap()
        );
    }

    @Test
    @DisplayName("9. Test Customer Booking Details Enriched with Worker Card")
    void testGetBookingById_EnrichedWorkerCard() {
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));
        when(workerProfileRepository.findById(workerUuid)).thenReturn(Optional.of(sampleWorker));

        BookingResponseDTO res = bookingService.getBookingById(1001L, customerId);

        assertNotNull(res);
        assertEquals(1001L, res.getId());
        assertEquals("4827", res.getStartPin()); // Revealed to customer on ACCEPTED

        // Verify Worker Summary Card
        assertNotNull(res.getWorker());
        assertEquals("Ravi Kumar", res.getWorker().getName());
        assertEquals("Electrician", res.getWorker().getService());
        assertEquals(BigDecimal.valueOf(4.85), res.getWorker().getRatingAvg());
        assertEquals(127, res.getWorker().getCompletedJobs());
        assertEquals(5, res.getWorker().getExperienceYears());
        assertTrue(res.getWorker().getVerified());
        assertNotNull(res.getWorker().getMaskedPhone());

        // Verify Distance & ETA
        assertNotNull(res.getDistanceKm());
        assertNotNull(res.getEtaMinutes());
        assertNotNull(res.getEtaText());
    }

    @Test
    @DisplayName("10. Test Worker Confirm Payment (Received) - Updates status to CONFIRMED and notifies customer")
    void testWorkerConfirmPayment_Received() {
        sampleBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));
        when(workerProfileRepository.findById(workerUuid)).thenReturn(Optional.of(sampleWorker));

        WorkerPaymentConfirmationRequestDTO request = WorkerPaymentConfirmationRequestDTO.builder()
                .received(true)
                .paymentMethod(com.salaryneeds.entity.enums.PaymentMethod.CASH)
                .amountReceived(BigDecimal.valueOf(450.00))
                .transactionReference("CASH-COLLECTED-1001")
                .remarks("Collected in full by hand")
                .build();

        PaymentConfirmationResponseDTO response = bookingService.confirmPayment(1001L, workerUuid.toString(), request);

        assertNotNull(response);
        assertEquals(com.salaryneeds.entity.enums.PaymentStatus.CONFIRMED, response.getPaymentStatus());
        assertEquals(com.salaryneeds.entity.enums.PaymentMethod.CASH, response.getPaymentMethod());
        assertEquals(BigDecimal.valueOf(450.00), response.getAmountReceived());
        assertNotNull(response.getConfirmedAt());

        // Verify booking entity updated
        assertEquals(com.salaryneeds.entity.enums.PaymentStatus.CONFIRMED, sampleBooking.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(450.00), sampleBooking.getPaymentReceivedAmount());

        // Verify customer received real-time payment confirmation notification
        verify(notificationService, times(1)).notifyCustomerBookingEvent(
                eq(customerId), eq(1001L), eq(BookingStatus.COMPLETED), eq("Payment Confirmed! ✅"), anyString(), anyMap()
        );
    }

    @Test
    @DisplayName("11. Test Worker Confirm Payment (Not Received / Dispute) - Updates status to NOT_RECEIVED")
    void testWorkerConfirmPayment_NotReceived() {
        sampleBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(sampleBooking));
        when(workerProfileRepository.findById(workerUuid)).thenReturn(Optional.of(sampleWorker));

        WorkerPaymentConfirmationRequestDTO request = WorkerPaymentConfirmationRequestDTO.builder()
                .received(false)
                .paymentMethod(com.salaryneeds.entity.enums.PaymentMethod.UPI)
                .amountReceived(BigDecimal.ZERO)
                .remarks("Customer UPI payment failed / not received")
                .build();

        PaymentConfirmationResponseDTO response = bookingService.confirmPayment(1001L, workerUuid.toString(), request);

        assertNotNull(response);
        assertEquals(com.salaryneeds.entity.enums.PaymentStatus.NOT_RECEIVED, response.getPaymentStatus());
        assertEquals(com.salaryneeds.entity.enums.PaymentStatus.NOT_RECEIVED, sampleBooking.getPaymentStatus());

        verify(notificationService, times(1)).notifyCustomerBookingEvent(
                eq(customerId), eq(1001L), eq(BookingStatus.COMPLETED), eq("Payment Issue Reported ⚠️"), anyString(), anyMap()
        );
    }
}

