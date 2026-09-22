package com.salaryneeds;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Address;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.entity.enums.DiscountType;
import com.salaryneeds.exception.InvalidBookingStateException;
import com.salaryneeds.repository.AddressRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.service.BookingServiceImpl;
import com.salaryneeds.service.CouponService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.salaryneeds.service.WorkerMatchingService workerMatchingService;

    @Mock
    private com.salaryneeds.repository.WorkerProfileRepository workerProfileRepository;

    @Mock
    private com.salaryneeds.repository.WorkerLocationRepository workerLocationRepository;

    @Mock
    private com.salaryneeds.service.NotificationService notificationService;

    @Mock
    private com.salaryneeds.repository.CartRepository cartRepository;

    @Mock
    private com.salaryneeds.repository.BookingItemRepository bookingItemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private String customerId;
    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID().toString();

        sampleBooking = Booking.builder()
                .id(101L)
                .customerId(customerId)
                .serviceId(1L)
                .serviceName("Deep Home Cleaning")
                .bookingDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1000.00))
                .discountAmount(BigDecimal.ZERO)
                .payableAmount(BigDecimal.valueOf(1000.00))
                .slotId("SLOT-0912")
                .scheduledTime("09:00 AM - 12:00 PM")
                .startPinHash("hashedPin")
                .startPinEncrypted("4589")
                .startPinVerified(false)
                .build();
    }

    @Test
    @DisplayName("Create booking - Generates 4-digit PIN and masks PIN in response (status PENDING)")
    void testCreateBooking_Success_PinMasked() {
        BookingCreateRequestDTO request = BookingCreateRequestDTO.builder()
                .customerId(customerId)
                .serviceId(1L)
                .serviceName("Deep Home Cleaning")
                .bookingDate(LocalDate.now().plusDays(1))
                .slotId("SLOT-0912")
                .scheduledTime("09:00 AM - 12:00 PM")
                .totalAmount(BigDecimal.valueOf(1000.00))
                .build();

        when(customerRepository.existsById(any(UUID.class))).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(101L);
            return b;
        });

        BookingResponseDTO response = bookingService.createBooking(request, customerId);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        // PIN must be masked / null on creation
        assertNull(response.getStartPin());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Create booking with Coupon - Applies discount correctly")
    void testCreateBooking_WithCoupon() {
        BookingCreateRequestDTO request = BookingCreateRequestDTO.builder()
                .customerId(customerId)
                .serviceId(1L)
                .serviceName("Deep Home Cleaning")
                .bookingDate(LocalDate.now().plusDays(1))
                .totalAmount(BigDecimal.valueOf(1000.00))
                .couponCode("WELCOME50")
                .build();

        CouponValidationResponseDTO couponResponse = CouponValidationResponseDTO.builder()
                .valid(true)
                .couponCode("WELCOME50")
                .discountAmount(BigDecimal.valueOf(250.00))
                .finalAmount(BigDecimal.valueOf(750.00))
                .build();

        when(customerRepository.existsById(any(UUID.class))).thenReturn(true);
        when(couponService.validateCoupon(eq("WELCOME50"), any(), any(), any())).thenReturn(couponResponse);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDTO response = bookingService.createBooking(request, customerId);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(250.00), response.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(750.00), response.getPayableAmount());
        verify(couponService, times(1)).recordCouponUsage("WELCOME50");
    }

    @Test
    @DisplayName("Security Rule: Start PIN is null when status is PENDING (not yet accepted by worker)")
    void testGetBooking_PinMaskedWhenPending() {
        sampleBooking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(101L)).thenReturn(Optional.of(sampleBooking));

        BookingResponseDTO response = bookingService.getBookingById(101L, customerId);

        assertNotNull(response);
        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertNull(response.getStartPin(), "PIN must be null/masked when status is PENDING");
    }

    @Test
    @DisplayName("Security Rule: Start PIN is REVEALED when worker accepts booking (status ACCEPTED)")
    void testGetBooking_PinRevealedWhenAccepted() {
        sampleBooking.setStatus(BookingStatus.ACCEPTED);
        when(bookingRepository.findById(101L)).thenReturn(Optional.of(sampleBooking));

        BookingResponseDTO response = bookingService.getBookingById(101L, customerId);

        assertNotNull(response);
        assertEquals(BookingStatus.ACCEPTED, response.getStatus());
        assertEquals("4589", response.getStartPin(), "PIN must be revealed when worker accepts booking");
    }

    @Test
    @DisplayName("Cancel booking - Success with full refund when PENDING")
    void testCancelBooking_Pending_FullRefund() {
        sampleBooking.setStatus(BookingStatus.PENDING);
        sampleBooking.setPayableAmount(BigDecimal.valueOf(1000.00));
        when(bookingRepository.findById(101L)).thenReturn(Optional.of(sampleBooking));

        BookingCancelRequestDTO request = BookingCancelRequestDTO.builder()
                .reason("Change of plans")
                .build();

        CancelBookingResponseDTO response = bookingService.cancelBooking(101L, request, customerId);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals(BigDecimal.ZERO, response.getCancellationFee());
        assertEquals(BigDecimal.valueOf(1000.00), response.getRefundAmount());
    }

    @Test
    @DisplayName("Cancel booking - Applies cancellation fee when WORKER_ON_THE_WAY")
    void testCancelBooking_WorkerOnWay_FeeApplied() {
        sampleBooking.setStatus(BookingStatus.WORKER_ON_THE_WAY);
        sampleBooking.setPayableAmount(BigDecimal.valueOf(1000.00));
        when(bookingRepository.findById(101L)).thenReturn(Optional.of(sampleBooking));

        BookingCancelRequestDTO request = BookingCancelRequestDTO.builder()
                .reason("Emergency came up")
                .build();

        CancelBookingResponseDTO response = bookingService.cancelBooking(101L, request, customerId);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals(BigDecimal.valueOf(50.00), response.getCancellationFee());
        assertEquals(BigDecimal.valueOf(950.00), response.getRefundAmount());
    }

    @Test
    @DisplayName("Cancel booking - Rejects cancellation when already IN_PROGRESS")
    void testCancelBooking_InProgress_ThrowsException() {
        sampleBooking.setStatus(BookingStatus.IN_PROGRESS);
        when(bookingRepository.findById(101L)).thenReturn(Optional.of(sampleBooking));

        BookingCancelRequestDTO request = BookingCancelRequestDTO.builder()
                .reason("Do not want service now")
                .build();

        assertThrows(InvalidBookingStateException.class, () -> bookingService.cancelBooking(101L, request, customerId));
    }

    @Test
    @DisplayName("Dynamic Slots - Generates 4 slots")
    void testGetAvailableSlots() {
        List<SlotResponseDTO> slots = bookingService.getAvailableSlots(1L, LocalDate.now().plusDays(2));

        assertNotNull(slots);
        assertEquals(4, slots.size());
        assertTrue(slots.stream().anyMatch(s -> s.getSlotId().equals("SLOT-0912")));
        assertTrue(slots.stream().anyMatch(s -> s.getSlotId().equals("SLOT-1215")));
    }

    @Test
    @DisplayName("Dynamic Slots - Instamart-style cutoff matches slot end time")
    void testGetAvailableSlots_InstamartCutoff() {
        LocalDate testDate = LocalDate.now(BookingServiceImpl.BUSINESS_ZONE).plusDays(1);
        List<SlotResponseDTO> slots = bookingService.getAvailableSlots(1L, testDate);

        assertNotNull(slots);
        SlotResponseDTO slot1215 = slots.stream().filter(s -> s.getSlotId().equals("SLOT-1215")).findFirst().orElseThrow();
        assertEquals("15:00", slot1215.getEndTime());
        assertEquals(testDate.atTime(15, 0), slot1215.getCutoffTime());
        assertTrue(slot1215.getIsAvailable());
    }
}
