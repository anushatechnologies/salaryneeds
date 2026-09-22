package com.salaryneeds;

import com.salaryneeds.dto.ApplyCouponResponseDTO;
import com.salaryneeds.dto.CouponDTO;
import com.salaryneeds.dto.CouponValidateRequestDTO;
import com.salaryneeds.dto.CouponValidationResponseDTO;
import com.salaryneeds.dto.BookingResponseDTO;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.Coupon;
import com.salaryneeds.entity.CouponRedemption;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.entity.enums.DiscountType;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.CouponRedemptionRepository;
import com.salaryneeds.repository.CouponRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.service.CouponServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRedemptionRepository couponRedemptionRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Test
    @DisplayName("Step 1 & 2 Fail: Code not found returns 'Invalid coupon code'")
    void testValidateCoupon_InvalidCode() {
        when(couponRepository.findByCodeIgnoreCase("NONEXISTENT")).thenReturn(Optional.empty());

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("NONEXISTENT")
                .orderAmount(BigDecimal.valueOf(500.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertFalse(response.getValid());
        assertEquals("Invalid coupon code", response.getMessage());
    }

    @Test
    @DisplayName("Step 2 Fail: Active = false returns 'Coupon no longer available'")
    void testValidateCoupon_InactiveCoupon() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("INACTIVE10")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(10.00))
                .isActive(false)
                .build();

        when(couponRepository.findByCodeIgnoreCase("INACTIVE10")).thenReturn(Optional.of(coupon));

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("INACTIVE10")
                .orderAmount(BigDecimal.valueOf(500.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertFalse(response.getValid());
        assertEquals("Coupon no longer available", response.getMessage());
    }

    @Test
    @DisplayName("Step 3 Fail: ValidFrom in future returns 'Coupon not active yet'")
    void testValidateCoupon_NotStartedCoupon() {
        Coupon coupon = Coupon.builder()
                .id(2L)
                .code("FUTURE20")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(20.00))
                .validFrom(LocalDateTime.now().plusDays(2))
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("FUTURE20")).thenReturn(Optional.of(coupon));

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("FUTURE20")
                .orderAmount(BigDecimal.valueOf(500.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertFalse(response.getValid());
        assertEquals("Coupon not active yet", response.getMessage());
    }

    @Test
    @DisplayName("Step 3 Fail: ValidUntil in past returns 'Coupon has expired'")
    void testValidateCoupon_ExpiredCoupon() {
        Coupon coupon = Coupon.builder()
                .id(3L)
                .code("EXPIRED30")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(30.00))
                .validUntil(LocalDateTime.now().minusMinutes(5))
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("EXPIRED30")).thenReturn(Optional.of(coupon));

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("EXPIRED30")
                .orderAmount(BigDecimal.valueOf(500.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertFalse(response.getValid());
        assertEquals("Coupon has expired", response.getMessage());
    }

    @Test
    @DisplayName("Step 4 Fail: Below minimum order value returns 'Minimum order value not met'")
    void testValidateCoupon_MinOrderValueNotMet() {
        Coupon coupon = Coupon.builder()
                .id(4L)
                .code("MIN500")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(50.00))
                .minOrderAmount(BigDecimal.valueOf(500.00))
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("MIN500")).thenReturn(Optional.of(coupon));

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("MIN500")
                .orderAmount(BigDecimal.valueOf(300.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertFalse(response.getValid());
        assertEquals("Minimum order value not met", response.getMessage());
    }

    @Test
    @DisplayName("Step 5 Fail: Usage limit reached returns 'Coupon usage limit reached'")
    void testValidateCoupon_UsageLimitReached() {
        Coupon coupon = Coupon.builder()
                .id(5L)
                .code("LIMITREACHED")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(50.00))
                .usageLimit(2)
                .usedCount(2)
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("LIMITREACHED")).thenReturn(Optional.of(coupon));
        when(couponRedemptionRepository.countByCouponId(5L)).thenReturn(2L);

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("LIMITREACHED")
                .orderAmount(BigDecimal.valueOf(500.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertFalse(response.getValid());
        assertEquals("Coupon usage limit reached", response.getMessage());
    }

    @Test
    @DisplayName("Step 6 & 7: Percentage discount with max cap & order price cap")
    void testValidateCoupon_PercentageWithCapSuccess() {
        Coupon coupon = Coupon.builder()
                .id(6L)
                .code("WELCOME50")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(50.00))
                .minOrderAmount(BigDecimal.valueOf(400.00))
                .maxDiscountAmount(BigDecimal.valueOf(250.00))
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .usageLimit(100)
                .usedCount(5)
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("WELCOME50")).thenReturn(Optional.of(coupon));
        when(couponRedemptionRepository.countByCouponId(6L)).thenReturn(5L);

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("WELCOME50")
                .orderAmount(BigDecimal.valueOf(1000.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertTrue(response.getValid());
        // 50% of 1000 is 500, but cap is 250
        assertEquals(BigDecimal.valueOf(250.00), response.getDiscountApplied());
        assertEquals(BigDecimal.valueOf(750.00), response.getFinalPayablePrice());
    }

    @Test
    @DisplayName("Step 8 & 9: Apply coupon to booking successfully")
    void testApplyCouponToBooking_Success() {
        String customerId = UUID.randomUUID().toString();
        Booking booking = Booking.builder()
                .id(100L)
                .customerId(customerId)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(600.00))
                .payableAmount(BigDecimal.valueOf(600.00))
                .build();

        Coupon coupon = Coupon.builder()
                .id(7L)
                .code("FLAT100")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(100.00))
                .minOrderAmount(BigDecimal.valueOf(300.00))
                .usageLimit(10)
                .usedCount(0)
                .isActive(true)
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(couponRedemptionRepository.existsByBookingId(100L)).thenReturn(false);
        when(couponRepository.findByCodeIgnoreCase("FLAT100")).thenReturn(Optional.of(coupon));
        when(couponRedemptionRepository.countByCouponId(7L)).thenReturn(0L);

        ApplyCouponResponseDTO response = couponService.applyCouponToBooking(100L, "FLAT100", customerId);

        assertNotNull(response);
        assertEquals(100L, response.getBookingId());
        assertEquals("FLAT100", response.getCouponCode());
        assertEquals(0, BigDecimal.valueOf(600.00).compareTo(response.getOriginalPrice()));
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(response.getDiscountApplied()));
        assertEquals(0, BigDecimal.valueOf(500.00).compareTo(response.getFinalPayablePrice()));

        verify(couponRedemptionRepository, times(1)).save(any(CouponRedemption.class));
        verify(bookingRepository, times(1)).save(booking);
        verify(couponRepository, times(1)).save(coupon);
    }

    @Test
    @DisplayName("Step 8 Fail: Cannot apply second coupon when booking already has one")
    void testApplyCouponToBooking_AlreadyHasCoupon() {
        Booking booking = Booking.builder()
                .id(101L)
                .customerId("cust1")
                .build();

        when(bookingRepository.findById(101L)).thenReturn(Optional.of(booking));
        when(couponRedemptionRepository.existsByBookingId(101L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                couponService.applyCouponToBooking(101L, "CODE2", "cust1")
        );

        assertEquals("Only one coupon allowed per booking", ex.getMessage());
    }

    @Test
    @DisplayName("Step 10: Remove coupon from booking restores original price")
    void testRemoveCouponFromBooking() {
        Booking booking = Booking.builder()
                .id(102L)
                .customerId("cust2")
                .totalAmount(BigDecimal.valueOf(400.00))
                .discountAmount(BigDecimal.valueOf(100.00))
                .payableAmount(BigDecimal.valueOf(300.00))
                .couponCode("REMOVE100")
                .build();

        Coupon coupon = Coupon.builder()
                .id(8L)
                .code("REMOVE100")
                .usedCount(1)
                .build();

        CouponRedemption redemption = CouponRedemption.builder()
                .booking(booking)
                .coupon(coupon)
                .discountApplied(BigDecimal.valueOf(100.00))
                .build();

        when(bookingRepository.findById(102L)).thenReturn(Optional.of(booking));
        when(couponRedemptionRepository.findByBookingId(102L)).thenReturn(Optional.of(redemption));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDTO response = couponService.removeCouponFromBooking(102L);

        assertNotNull(response);
        assertNull(booking.getCouponCode());
        assertEquals(0, BigDecimal.ZERO.compareTo(booking.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(400.00).compareTo(booking.getPayableAmount()));
        verify(couponRedemptionRepository, times(1)).deleteByBookingId(102L);
    }

    @Test
    @DisplayName("Reverse coupon redemption on booking cancellation")
    void testReverseCouponRedemption() {
        Coupon coupon = Coupon.builder().id(9L).usedCount(2).build();
        CouponRedemption redemption = CouponRedemption.builder().coupon(coupon).build();

        when(couponRedemptionRepository.findByBookingId(103L)).thenReturn(Optional.of(redemption));

        couponService.reverseCouponRedemption(103L);

        assertEquals(1, coupon.getUsedCount());
        verify(couponRepository, times(1)).save(coupon);
        verify(couponRedemptionRepository, times(1)).deleteByBookingId(103L);
    }

    @Test
    @DisplayName("Admin CRUD operations")
    void testAdminCrud() {
        CouponDTO dto = CouponDTO.builder()
                .code("ADMIN100")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(100.00))
                .active(true)
                .build();

        Coupon savedEntity = Coupon.builder()
                .id(20L)
                .code("ADMIN100")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(100.00))
                .isActive(true)
                .build();

        when(couponRepository.existsByCodeIgnoreCase("ADMIN100")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedEntity);
        when(couponRepository.findById(20L)).thenReturn(Optional.of(savedEntity));
        when(couponRepository.findAll()).thenReturn(List.of(savedEntity));
        when(couponRepository.existsById(20L)).thenReturn(true);

        CouponDTO created = couponService.createCoupon(dto);
        assertEquals(20L, created.getId());
        assertEquals("ADMIN100", created.getCode());

        CouponDTO fetched = couponService.getCouponById(20L);
        assertEquals("ADMIN100", fetched.getCode());

        List<CouponDTO> list = couponService.getAllCoupons();
        assertEquals(1, list.size());

        couponService.deleteCoupon(20L);
        verify(couponRepository, times(1)).deleteById(20L);
    }
}
