package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    CouponValidationResponseDTO validateCoupon(CouponValidateRequestDTO request);

    CouponValidationResponseDTO validateCoupon(String code, BigDecimal orderAmount, Long serviceId, String customerId);

    ApplyCouponResponseDTO applyCouponToBooking(Long bookingId, String code, String customerId);

    BookingResponseDTO removeCouponFromBooking(Long bookingId);

    void reverseCouponRedemption(Long bookingId);

    CouponDTO createCoupon(CouponDTO couponDTO);

    List<CouponDTO> getAllCoupons();

    List<CouponDTO> getAvailableCoupons();

    PageResponseDTO<CouponDTO> getAllCoupons(Boolean active, String code, Pageable pageable);

    CouponDTO getCouponById(Long id);

    CouponDTO updateCoupon(Long id, CouponDTO couponDTO);

    CouponDTO patchCoupon(Long id, CouponDTO couponDTO);

    CouponDTO activateCoupon(Long id);

    CouponDTO deactivateCoupon(Long id);

    CouponDTO updateCouponStatus(Long id, Boolean active);

    CouponRedemptionsResponseDTO getCouponRedemptions(Long id, Pageable pageable);

    void deleteCoupon(Long id);

    CouponDTO getCouponByCode(String code);

    ApplyCouponResponseDTO applyCouponDirect(ApplyCouponRequestDTO request, String fallbackCustomerId);

    void recordCouponUsage(String code);
}
