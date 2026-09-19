package com.salaryneeds.controller;

import com.salaryneeds.dto.*;
import com.salaryneeds.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // Public: List available coupons
    @GetMapping({"/api/coupons", "/coupons"})
    public ResponseEntity<java.util.List<CouponDTO>> getAvailableCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    // Public: Get coupon by code
    @GetMapping({"/api/coupons/{code}", "/coupons/{code}"})
    public ResponseEntity<CouponDTO> getCouponByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(couponService.getCouponByCode(code));
    }

    // 1. POST /api/coupons/validate — Previews discount for draft booking without writing to DB
    @PostMapping({"/api/coupons/validate", "/coupons/validate", "/api/bookings/coupons/validate"})
    public ResponseEntity<CouponValidationResponseDTO> validateCoupon(
            @Valid @RequestBody CouponValidateRequestDTO request
    ) {
        return ResponseEntity.ok(couponService.validateCoupon(request));
    }

    // Public / Booking: Direct apply coupon
    @PostMapping({"/api/coupons/apply", "/coupons/apply"})
    public ResponseEntity<ApplyCouponResponseDTO> applyCouponDirect(
            @Valid @RequestBody ApplyCouponRequestDTO request,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader
    ) {
        return ResponseEntity.ok(couponService.applyCouponDirect(request, customerIdHeader));
    }

    // 2. POST /api/bookings/{id}/apply-coupon — Validates, saves redemption & updates booking price in 1 transaction
    @PostMapping({"/api/bookings/{id}/apply-coupon", "/bookings/{id}/apply-coupon"})
    public ResponseEntity<ApplyCouponResponseDTO> applyCouponToBooking(
            @PathVariable("id") Long bookingId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @Valid @RequestBody ApplyCouponRequestDTO request
    ) {
        String effectiveCustomerId = (request.getCustomerId() != null && !request.getCustomerId().isBlank())
                ? request.getCustomerId()
                : customerIdHeader;
        ApplyCouponResponseDTO response = couponService.applyCouponToBooking(bookingId, request.getCode(), effectiveCustomerId);
        return ResponseEntity.ok(response);
    }

    // 3. DELETE /api/bookings/{id}/coupon — Removes coupon before payment and restores original price
    @DeleteMapping({"/api/bookings/{id}/coupon", "/bookings/{id}/coupon"})
    public ResponseEntity<BookingResponseDTO> removeCouponFromBooking(
            @PathVariable("id") Long bookingId
    ) {
        BookingResponseDTO response = couponService.removeCouponFromBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    // 4. POST /api/admin/coupons — Creates a coupon with its discount rules
    @PostMapping({"/api/admin/coupons", "/admin/coupons"})
    public ResponseEntity<CouponDTO> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        CouponDTO created = couponService.createCoupon(couponDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 5. GET /api/admin/coupons — Lists coupons with filters and pagination
    @GetMapping({"/api/admin/coupons", "/admin/coupons"})
    public ResponseEntity<PageResponseDTO<CouponDTO>> getAllCoupons(
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(couponService.getAllCoupons(active, code, pageable));
    }

    // 6. GET /api/admin/coupons/{id} — Returns the details of one coupon
    @GetMapping({"/api/admin/coupons/{id}", "/admin/coupons/{id}"})
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    // 7. PATCH /api/admin/coupons/{id} — Edits a coupon's rules or toggles active (the kill-switch)
    @PatchMapping({"/api/admin/coupons/{id}", "/admin/coupons/{id}"})
    public ResponseEntity<CouponDTO> patchCoupon(
            @PathVariable Long id,
            @RequestBody CouponDTO couponDTO
    ) {
        return ResponseEntity.ok(couponService.patchCoupon(id, couponDTO));
    }

    @PatchMapping({"/api/admin/coupons/{id}/status", "/admin/coupons/{id}/status"})
    public ResponseEntity<CouponDTO> toggleCouponStatus(@PathVariable Long id) {
        CouponDTO existing = couponService.getCouponById(id);
        boolean currentStatus = existing.getIsActive() != null ? existing.getIsActive() : true;
        existing.setIsActive(!currentStatus);
        return ResponseEntity.ok(couponService.patchCoupon(id, existing));
    }

    @PutMapping({"/api/admin/coupons/{id}", "/admin/coupons/{id}"})
    public ResponseEntity<CouponDTO> updateCoupon(
            @PathVariable Long id,
            @RequestBody CouponDTO couponDTO
    ) {
        return ResponseEntity.ok(couponService.updateCoupon(id, couponDTO));
    }

    // 8. GET /api/admin/coupons/{id}/redemptions — Returns redemption log and usage count for one coupon
    @GetMapping({"/api/admin/coupons/{id}/redemptions", "/admin/coupons/{id}/redemptions"})
    public ResponseEntity<CouponRedemptionsResponseDTO> getCouponRedemptions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "redeemedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(couponService.getCouponRedemptions(id, pageable));
    }

    @DeleteMapping({"/api/admin/coupons/{id}", "/admin/coupons/{id}"})
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
