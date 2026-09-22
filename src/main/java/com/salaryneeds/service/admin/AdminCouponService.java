package com.salaryneeds.service.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.CouponCreateRequestDTO;
import com.salaryneeds.dto.admin.CouponResponseDTO;
import com.salaryneeds.dto.admin.CouponUpdateRequestDTO;
import com.salaryneeds.entity.Coupon;
import com.salaryneeds.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCouponService {

    private final CouponRepository couponRepository;
    private final AuditLogService auditLogService;

    public PageResponseDTO<CouponResponseDTO> getAllCoupons(String search, Boolean isActive, Pageable pageable) {
        Page<Coupon> page;
        if (search != null && !search.trim().isEmpty()) {
            page = couponRepository.findByCodeContainingIgnoreCase(search.trim(), pageable);
        } else if (isActive != null) {
            page = couponRepository.findByIsActive(isActive, pageable);
        } else {
            page = couponRepository.findAll(pageable);
        }

        List<CouponResponseDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<CouponResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    public CouponResponseDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + id));
        return toDTO(coupon);
    }

    @Transactional
    public CouponResponseDTO createCoupon(CouponCreateRequestDTO request, UUID adminId) {
        String cleanCode = request.getCode().trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new IllegalArgumentException("Coupon code already exists: " + cleanCode);
        }

        Coupon coupon = Coupon.builder()
                .code(cleanCode)
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Coupon saved = couponRepository.save(coupon);

        auditLogService.log(adminId, "CREATE_COUPON", "COUPON", saved.getId().toString(),
                "Created coupon " + saved.getCode());

        return toDTO(saved);
    }

    @Transactional
    public CouponResponseDTO updateCoupon(Long id, CouponUpdateRequestDTO request, UUID adminId) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + id));

        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            String newCode = request.getCode().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(coupon.getCode()) && couponRepository.existsByCodeIgnoreCase(newCode)) {
                throw new IllegalArgumentException("Coupon code already exists: " + newCode);
            }
            coupon.setCode(newCode);
        }
        if (request.getDescription() != null) {
            coupon.setDescription(request.getDescription());
        }
        if (request.getDiscountType() != null) {
            coupon.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) {
            coupon.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMinOrderAmount() != null) {
            coupon.setMinOrderAmount(request.getMinOrderAmount());
        }
        if (request.getMaxDiscountAmount() != null) {
            coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getValidFrom() != null) {
            coupon.setValidFrom(request.getValidFrom());
        }
        if (request.getValidUntil() != null) {
            coupon.setValidUntil(request.getValidUntil());
        }
        if (request.getUsageLimit() != null) {
            coupon.setUsageLimit(request.getUsageLimit());
        }
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }

        Coupon updated = couponRepository.save(coupon);

        auditLogService.log(adminId, "UPDATE_COUPON", "COUPON", updated.getId().toString(),
                "Updated coupon " + updated.getCode());

        return toDTO(updated);
    }

    @Transactional
    public CouponResponseDTO toggleCouponStatus(Long id, UUID adminId) {
        return updateCouponStatus(id, null, adminId);
    }

    @Transactional
    public CouponResponseDTO activateCoupon(Long id, UUID adminId) {
        return updateCouponStatus(id, true, adminId);
    }

    @Transactional
    public CouponResponseDTO deactivateCoupon(Long id, UUID adminId) {
        return updateCouponStatus(id, false, adminId);
    }

    @Transactional
    public CouponResponseDTO updateCouponStatus(Long id, Boolean active, UUID adminId) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + id));

        boolean newStatus = active != null ? active : !Boolean.TRUE.equals(coupon.getIsActive());
        coupon.setIsActive(newStatus);
        Coupon updated = couponRepository.save(coupon);

        String action = newStatus ? "ACTIVATE_COUPON" : "DEACTIVATE_COUPON";
        auditLogService.log(adminId, action, "COUPON", updated.getId().toString(),
                (newStatus ? "Activated" : "Deactivated") + " coupon " + updated.getCode());

        return toDTO(updated);
    }

    @Transactional
    public void deleteCoupon(Long id, UUID adminId) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + id));

        couponRepository.delete(coupon);

        auditLogService.log(adminId, "DELETE_COUPON", "COUPON", id.toString(),
                "Deleted coupon " + coupon.getCode());
    }

    private CouponResponseDTO toDTO(Coupon coupon) {
        return CouponResponseDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .isActive(coupon.getIsActive())
                .isAccepted(coupon.getIsActive())
                .isCurrentlyValid(coupon.isCurrentlyValid())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}
