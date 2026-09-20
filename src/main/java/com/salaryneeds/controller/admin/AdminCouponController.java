package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.CouponCreateRequestDTO;
import com.salaryneeds.dto.admin.CouponResponseDTO;
import com.salaryneeds.dto.admin.CouponUpdateRequestDTO;
import com.salaryneeds.service.admin.AdminCouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/admin/coupon-management", "/admin/coupon-management"})
@RequiredArgsConstructor
public class AdminCouponController {

    private final AdminCouponService couponService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<CouponResponseDTO>> getAllCoupons(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(couponService.getAllCoupons(search, isActive, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    @PostMapping
    public ResponseEntity<CouponResponseDTO> createCoupon(
            @Valid @RequestBody CouponCreateRequestDTO request,
            HttpServletRequest httpRequest) {
        UUID adminId = (UUID) httpRequest.getAttribute("adminId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponService.createCoupon(request, adminId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponUpdateRequestDTO request,
            HttpServletRequest httpRequest) {
        UUID adminId = (UUID) httpRequest.getAttribute("adminId");
        return ResponseEntity.ok(couponService.updateCoupon(id, request, adminId));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CouponResponseDTO> toggleCouponStatus(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        UUID adminId = (UUID) httpRequest.getAttribute("adminId");
        return ResponseEntity.ok(couponService.toggleCouponStatus(id, adminId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        UUID adminId = (UUID) httpRequest.getAttribute("adminId");
        couponService.deleteCoupon(id, adminId);
        return ResponseEntity.noContent().build();
    }
}
