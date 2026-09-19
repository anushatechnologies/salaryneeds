package com.salaryneeds.service;

import com.salaryneeds.dto.CouponValidateRequestDTO;
import com.salaryneeds.dto.CouponValidationResponseDTO;
import com.salaryneeds.entity.Coupon;
import com.salaryneeds.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponseDTO validateCoupon(CouponValidateRequestDTO request) {
        return validateCoupon(request.getCode(), request.getOrderAmount(), request.getServiceId(), request.getCustomerId());
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponseDTO validateCoupon(String code, BigDecimal orderAmount, Long serviceId, String customerId) {
        if (code == null || code.isBlank()) {
            return CouponValidationResponseDTO.builder()
                    .valid(false)
                    .orderAmount(orderAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .message("Promo code cannot be empty")
                    .build();
        }

        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(code.trim());
        if (couponOpt.isEmpty()) {
            return CouponValidationResponseDTO.builder()
                    .valid(false)
                    .couponCode(code)
                    .orderAmount(orderAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .message("Invalid promo code: " + code)
                    .build();
        }

        Coupon coupon = couponOpt.get();

        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            return CouponValidationResponseDTO.builder()
                    .valid(false)
                    .couponCode(coupon.getCode())
                    .orderAmount(orderAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .message("Promo code is no longer active")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return CouponValidationResponseDTO.builder()
                    .valid(false)
                    .couponCode(coupon.getCode())
                    .orderAmount(orderAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .message("Promo code is not yet valid")
                    .build();
        }

        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return CouponValidationResponseDTO.builder()
                    .valid(false)
                    .couponCode(coupon.getCode())
                    .orderAmount(orderAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .message("Promo code has expired")
                    .build();
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return CouponValidationResponseDTO.builder()
                    .valid(false)
                    .couponCode(coupon.getCode())
                    .orderAmount(orderAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .message("Promo code usage limit reached")
                    .build();
        }

        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return CouponValidationResponseDTO.builder()
                    .valid(false)
                    .couponCode(coupon.getCode())
                    .orderAmount(orderAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .message("Minimum order amount of Rs " + coupon.getMinOrderAmount() + " required for this promo code")
                    .build();
        }

        BigDecimal discount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return CouponValidationResponseDTO.builder()
                .valid(true)
                .couponCode(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .orderAmount(orderAmount)
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .message("Promo code applied successfully! You saved Rs " + discount)
                .build();
    }

    @Override
    public void recordCouponUsage(String code) {
        if (code == null || code.isBlank()) return;
        couponRepository.findByCodeIgnoreCase(code.trim()).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() != null ? coupon.getUsedCount() + 1 : 1);
            couponRepository.save(coupon);
        });
    }
}
