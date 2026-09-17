package com.salaryneeds.service;

import com.salaryneeds.dto.CouponValidateRequestDTO;
import com.salaryneeds.dto.CouponValidationResponseDTO;

import java.math.BigDecimal;

public interface CouponService {

    CouponValidationResponseDTO validateCoupon(CouponValidateRequestDTO request);

    CouponValidationResponseDTO validateCoupon(String code, BigDecimal orderAmount, Long serviceId, String customerId);

    void recordCouponUsage(String code);
}
