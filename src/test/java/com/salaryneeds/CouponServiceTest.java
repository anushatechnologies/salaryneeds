package com.salaryneeds;

import com.salaryneeds.dto.CouponValidateRequestDTO;
import com.salaryneeds.dto.CouponValidationResponseDTO;
import com.salaryneeds.entity.Coupon;
import com.salaryneeds.entity.enums.DiscountType;
import com.salaryneeds.repository.CouponRepository;
import com.salaryneeds.service.CouponServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Test
    @DisplayName("Validate percentage coupon with max discount cap")
    void testValidateCoupon_PercentageWithCap() {
        Coupon coupon = Coupon.builder()
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

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("WELCOME50")
                .orderAmount(BigDecimal.valueOf(1000.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertTrue(response.getValid());
        // 50% of 1000 is 500, but cap is 250
        assertEquals(BigDecimal.valueOf(250.00), response.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(750.00), response.getFinalAmount());
    }

    @Test
    @DisplayName("Validate flat coupon")
    void testValidateCoupon_Flat() {
        Coupon coupon = Coupon.builder()
                .code("FESTIVE100")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(100.00))
                .minOrderAmount(BigDecimal.valueOf(500.00))
                .maxDiscountAmount(BigDecimal.valueOf(100.00))
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .usageLimit(100)
                .usedCount(0)
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("FESTIVE100")).thenReturn(Optional.of(coupon));

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("FESTIVE100")
                .orderAmount(BigDecimal.valueOf(600.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertTrue(response.getValid());
        assertEquals(BigDecimal.valueOf(100.00), response.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(500.00), response.getFinalAmount());
    }

    @Test
    @DisplayName("Validate coupon fails when order amount below minimum threshold")
    void testValidateCoupon_BelowMinThreshold() {
        Coupon coupon = Coupon.builder()
                .code("FESTIVE100")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(100.00))
                .minOrderAmount(BigDecimal.valueOf(500.00))
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("FESTIVE100")).thenReturn(Optional.of(coupon));

        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("FESTIVE100")
                .orderAmount(BigDecimal.valueOf(300.00))
                .build();

        CouponValidationResponseDTO response = couponService.validateCoupon(request);

        assertFalse(response.getValid());
        assertTrue(response.getMessage().contains("Minimum order amount"));
    }
}
