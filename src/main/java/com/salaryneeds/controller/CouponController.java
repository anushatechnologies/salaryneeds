package com.salaryneeds.controller;

import com.salaryneeds.dto.CouponValidateRequestDTO;
import com.salaryneeds.dto.CouponValidationResponseDTO;
import com.salaryneeds.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/api/coupons/validate")
    public ResponseEntity<CouponValidationResponseDTO> validateCoupon(
            @Valid @RequestBody CouponValidateRequestDTO request
    ) {
        return ResponseEntity.ok(couponService.validateCoupon(request));
    }

    @PostMapping("/api/bookings/coupons/validate")
    public ResponseEntity<CouponValidationResponseDTO> validateBookingCoupon(
            @Valid @RequestBody CouponValidateRequestDTO request
    ) {
        return ResponseEntity.ok(couponService.validateCoupon(request));
    }
}
