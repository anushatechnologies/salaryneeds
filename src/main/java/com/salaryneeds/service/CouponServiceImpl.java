package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.Coupon;
import com.salaryneeds.entity.CouponRedemption;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.exception.BookingNotFoundException;
import com.salaryneeds.exception.CouponNotFoundException;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.CouponRedemptionRepository;
import com.salaryneeds.repository.CouponRepository;
import com.salaryneeds.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.salaryneeds.entity.enums.BookingStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponseDTO validateCoupon(CouponValidateRequestDTO request) {
        BigDecimal orderAmount = request.getOrderAmount();
        return validateCoupon(request.getCode(), orderAmount, request.getServiceId(), request.getCustomerId(), request.getBookingId());
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponseDTO validateCoupon(String code, BigDecimal orderAmount, Long serviceId, String customerId) {
        return validateCoupon(code, orderAmount, serviceId, customerId, null);
    }

    @Transactional(readOnly = true)
    public CouponValidationResponseDTO validateCoupon(String code, BigDecimal orderAmount, Long serviceId, String customerId, Long bookingId) {
        // Step 1 — Lookup
        if (code == null || code.isBlank()) {
            return buildValidationResponse(false, code, orderAmount, BigDecimal.ZERO, orderAmount, "Invalid coupon code");
        }

        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(code.trim());
        if (couponOpt.isEmpty()) {
            return buildValidationResponse(false, code, orderAmount, BigDecimal.ZERO, orderAmount, "Invalid coupon code");
        }

        Coupon coupon = couponOpt.get();

        // Step 2 — Active check
        Boolean active = coupon.getActive() != null ? coupon.getActive() : coupon.getIsActive();
        if (!Boolean.TRUE.equals(active)) {
            return buildValidationResponse(false, coupon.getCode(), orderAmount, BigDecimal.ZERO, orderAmount, "Coupon no longer available");
        }

        // Step 3 — Validity window check
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return buildValidationResponse(false, coupon.getCode(), orderAmount, BigDecimal.ZERO, orderAmount, "Coupon not active yet");
        }

        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return buildValidationResponse(false, coupon.getCode(), orderAmount, BigDecimal.ZERO, orderAmount, "Coupon has expired");
        }

        // Step 4 — Minimum order value check
        BigDecimal minOrder = coupon.getMinBookingValue() != null ? coupon.getMinBookingValue() : coupon.getMinOrderAmount();
        if (orderAmount == null || (minOrder != null && orderAmount.compareTo(minOrder) < 0)) {
            return buildValidationResponse(false, coupon.getCode(), orderAmount, BigDecimal.ZERO, orderAmount, "Minimum order value not met");
        }

        // Step 5 — Usage limit check & Booking unique constraint check
        long currentRedemptions = couponRedemptionRepository.countByCouponId(coupon.getId());
        int usedCount = Math.max((int) currentRedemptions, coupon.getUsedCount() != null ? coupon.getUsedCount() : 0);
        if (coupon.getUsageLimit() != null && usedCount >= coupon.getUsageLimit()) {
            return buildValidationResponse(false, coupon.getCode(), orderAmount, BigDecimal.ZERO, orderAmount, "Coupon usage limit reached");
        }

        // Per-user usage limit check
        if (coupon.getUsageLimitPerUser() != null && customerId != null && !customerId.isBlank()) {
            long userRedemptions = couponRedemptionRepository.countByCouponIdAndCustomerId(coupon.getId(), customerId);
            if (userRedemptions >= coupon.getUsageLimitPerUser()) {
                return buildValidationResponse(false, coupon.getCode(), orderAmount, BigDecimal.ZERO, orderAmount, "Per-user usage limit reached");
            }
        }

        if (bookingId != null && couponRedemptionRepository.existsByBookingId(bookingId)) {
            return buildValidationResponse(false, coupon.getCode(), orderAmount, BigDecimal.ZERO, orderAmount, "Only one coupon allowed per booking");
        }

        // Step 6 & 7 — Calculate discount capped at order price
        BigDecimal discount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        CouponValidationResponseDTO response = buildValidationResponse(true, coupon.getCode(), orderAmount, discount, finalAmount,
                "Promo code applied successfully! You saved Rs " + discount);
        response.setDescription(coupon.getDescription());
        response.setDiscountType(coupon.getDiscountType());
        response.setDiscountValue(coupon.getDiscountValue());
        return response;
    }

    @Override
    public ApplyCouponResponseDTO applyCouponToBooking(Long bookingId, String code, String customerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));

        if (couponRedemptionRepository.existsByBookingId(bookingId)) {
            throw new IllegalArgumentException("Only one coupon allowed per booking");
        }

        BigDecimal originalPrice = getBookingOriginalPrice(booking);

        String effectiveCustomerId = (customerId != null && !customerId.isBlank()) ? customerId : booking.getCustomerId();

        CouponValidationResponseDTO validation = validateCoupon(code, originalPrice, booking.getServiceId(), effectiveCustomerId, bookingId);
        if (!Boolean.TRUE.equals(validation.getValid())) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new CouponNotFoundException("Invalid coupon code"));

        BigDecimal discountApplied = validation.getDiscountApplied();
        BigDecimal finalPayablePrice = originalPrice.subtract(discountApplied);
        if (finalPayablePrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPayablePrice = BigDecimal.ZERO;
        }

        Customer customer = null;
        if (effectiveCustomerId != null && !effectiveCustomerId.isBlank()) {
            try {
                customer = customerRepository.findById(java.util.UUID.fromString(effectiveCustomerId)).orElse(null);
            } catch (Exception ignored) {}
        }

        CouponRedemption redemption = CouponRedemption.builder()
                .coupon(coupon)
                .customerId(effectiveCustomerId)
                .customer(customer)
                .booking(booking)
                .discountApplied(discountApplied)
                .redeemedAt(LocalDateTime.now())
                .build();

        couponRedemptionRepository.save(redemption);

        booking.setDiscountAmount(discountApplied);
        booking.setPayableAmount(finalPayablePrice);
        booking.setCouponCode(coupon.getCode());
        bookingRepository.save(booking);

        coupon.setUsedCount((coupon.getUsedCount() != null ? coupon.getUsedCount() : 0) + 1);
        couponRepository.save(coupon);

        return ApplyCouponResponseDTO.builder()
                .bookingId(booking.getId())
                .couponCode(coupon.getCode())
                .originalPrice(originalPrice)
                .discountApplied(discountApplied)
                .finalPayablePrice(finalPayablePrice)
                .message("Coupon applied successfully")
                .build();
    }

    @Override
    public BookingResponseDTO removeCouponFromBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));

        Optional<CouponRedemption> redemptionOpt = couponRedemptionRepository.findByBookingId(bookingId);
        if (redemptionOpt.isPresent()) {
            CouponRedemption redemption = redemptionOpt.get();
            Coupon coupon = redemption.getCoupon();
            if (coupon != null && coupon.getUsedCount() != null && coupon.getUsedCount() > 0) {
                coupon.setUsedCount(coupon.getUsedCount() - 1);
                couponRepository.save(coupon);
            }
            couponRedemptionRepository.deleteByBookingId(bookingId);
        }

        BigDecimal originalPrice = getBookingOriginalPrice(booking);
        booking.setDiscountAmount(BigDecimal.ZERO);
        booking.setPayableAmount(originalPrice);
        booking.setCouponCode(null);
        Booking updatedBooking = bookingRepository.save(booking);

        return mapToBookingResponseDTO(updatedBooking);
    }

    @Override
    public void reverseCouponRedemption(Long bookingId) {
        if (bookingId == null) return;
        couponRedemptionRepository.findByBookingId(bookingId).ifPresent(redemption -> {
            Coupon coupon = redemption.getCoupon();
            if (coupon != null && coupon.getUsedCount() != null && coupon.getUsedCount() > 0) {
                coupon.setUsedCount(coupon.getUsedCount() - 1);
                couponRepository.save(coupon);
            }
            couponRedemptionRepository.deleteByBookingId(bookingId);
        });
    }

    @Override
    public CouponDTO createCoupon(CouponDTO couponDTO) {
        if (couponDTO.getCode() != null && couponRepository.existsByCodeIgnoreCase(couponDTO.getCode().trim())) {
            throw new IllegalArgumentException("Coupon code already exists: " + couponDTO.getCode());
        }
        Coupon coupon = mapToEntity(couponDTO);
        Coupon saved = couponRepository.save(coupon);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponDTO> getAvailableCoupons() {
        return couponRepository.findByIsActive(true).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CouponDTO> getAllCoupons(Boolean active, String code, Pageable pageable) {
        Page<Coupon> page;
        if (active != null && code != null && !code.isBlank()) {
            page = couponRepository.findByIsActiveAndCodeContainingIgnoreCase(active, code.trim(), pageable);
        } else if (active != null) {
            page = couponRepository.findByIsActive(active, pageable);
        } else if (code != null && !code.isBlank()) {
            page = couponRepository.findByCodeContainingIgnoreCase(code.trim(), pageable);
        } else {
            page = couponRepository.findAll(pageable);
        }

        List<CouponDTO> content = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<CouponDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + id));
        return mapToDTO(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDTO getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with code: " + code));
        Boolean active = coupon.getActive() != null ? coupon.getActive() : coupon.getIsActive();
        if (!Boolean.TRUE.equals(active)) {
            throw new CouponNotFoundException("Coupon not found or inactive: " + code);
        }
        return mapToDTO(coupon);
    }

    @Override
    public ApplyCouponResponseDTO applyCouponDirect(ApplyCouponRequestDTO request, String fallbackCustomerId) {
        String effectiveCustomerId = (request.getCustomerId() != null && !request.getCustomerId().isBlank())
                ? request.getCustomerId()
                : (fallbackCustomerId != null ? fallbackCustomerId : "cust-default");

        Long bookingId = request.getBookingId();
        if (bookingId == null || !bookingRepository.existsById(bookingId)) {
            BigDecimal amt = request.getBookingAmount() != null ? request.getBookingAmount() : BigDecimal.valueOf(1000.00);
            Booking b = Booking.builder()
                    .customerId(effectiveCustomerId)
                    .totalAmount(amt)
                    .payableAmount(amt)
                    .totalPrice(amt)
                    .basePrice(amt)
                    .bookingDate(LocalDate.now())
                    .status(BookingStatus.PENDING)
                    .serviceName("General Service")
                    .build();
            Booking savedBooking = bookingRepository.save(b);
            bookingId = savedBooking.getId();
        }
        return applyCouponToBooking(bookingId, request.getCode(), effectiveCustomerId);
    }

    @Override
    public CouponDTO updateCoupon(Long id, CouponDTO couponDTO) {
        Coupon existing = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + id));

        if (couponDTO.getCode() != null && !couponDTO.getCode().equalsIgnoreCase(existing.getCode())) {
            if (couponRepository.existsByCodeIgnoreCase(couponDTO.getCode().trim())) {
                throw new IllegalArgumentException("Coupon code already exists: " + couponDTO.getCode());
            }
            existing.setCode(couponDTO.getCode().trim());
        }

        if (couponDTO.getDescription() != null) existing.setDescription(couponDTO.getDescription());
        if (couponDTO.getDiscountType() != null) existing.setDiscountType(couponDTO.getDiscountType());
        if (couponDTO.getDiscountValue() != null) existing.setDiscountValue(couponDTO.getDiscountValue());
        if (couponDTO.getMinBookingValue() != null) existing.setMinBookingValue(couponDTO.getMinBookingValue());
        if (couponDTO.getMaxDiscount() != null) existing.setMaxDiscount(couponDTO.getMaxDiscount());
        if (couponDTO.getValidFrom() != null) existing.setValidFrom(couponDTO.getValidFrom());
        if (couponDTO.getValidUntil() != null) existing.setValidUntil(couponDTO.getValidUntil());
        if (couponDTO.getUsageLimit() != null) existing.setUsageLimit(couponDTO.getUsageLimit());
        if (couponDTO.getUsageLimitPerUser() != null) existing.setUsageLimitPerUser(couponDTO.getUsageLimitPerUser());
        if (couponDTO.getActive() != null) existing.setActive(couponDTO.getActive());

        Coupon updated = couponRepository.save(existing);
        return mapToDTO(updated);
    }

    @Override
    public CouponDTO patchCoupon(Long id, CouponDTO couponDTO) {
        return updateCoupon(id, couponDTO);
    }

    @Override
    public CouponDTO activateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + id));
        coupon.setIsActive(true);
        coupon.setActive(true);
        Coupon updated = couponRepository.save(coupon);
        return mapToDTO(updated);
    }

    @Override
    public CouponDTO deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + id));
        coupon.setIsActive(false);
        coupon.setActive(false);
        Coupon updated = couponRepository.save(coupon);
        return mapToDTO(updated);
    }

    @Override
    public CouponDTO updateCouponStatus(Long id, Boolean active) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + id));
        boolean targetStatus = active != null ? active : !Boolean.TRUE.equals(coupon.getIsActive());
        coupon.setIsActive(targetStatus);
        coupon.setActive(targetStatus);
        Coupon updated = couponRepository.save(coupon);
        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponRedemptionsResponseDTO getCouponRedemptions(Long id, Pageable pageable) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + id));

        Page<CouponRedemption> page = couponRedemptionRepository.findByCouponId(id, pageable);

        List<CouponRedemptionDTO> redemptionDTOs = page.getContent().stream()
                .map(r -> CouponRedemptionDTO.builder()
                        .id(r.getId())
                        .couponId(r.getCoupon().getId())
                        .customerId(r.getCustomerId())
                        .bookingId(r.getBooking() != null ? r.getBooking().getId() : null)
                        .discountApplied(r.getDiscountApplied())
                        .redeemedAt(r.getRedeemedAt())
                        .build())
                .collect(Collectors.toList());

        PageResponseDTO<CouponRedemptionDTO> redemptionsPage = PageResponseDTO.<CouponRedemptionDTO>builder()
                .content(redemptionDTOs)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();

        int currentUsage = (int) couponRedemptionRepository.countByCouponId(id);

        return CouponRedemptionsResponseDTO.builder()
                .couponId(coupon.getId())
                .couponCode(coupon.getCode())
                .usageCount(Math.max(currentUsage, coupon.getUsedCount() != null ? coupon.getUsedCount() : 0))
                .usageLimit(coupon.getUsageLimit())
                .redemptions(redemptionsPage)
                .build();
    }

    @Override
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new CouponNotFoundException("Coupon not found with ID: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Override
    public void recordCouponUsage(String code) {
        if (code == null || code.isBlank()) return;
        couponRepository.findByCodeIgnoreCase(code.trim()).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() != null ? coupon.getUsedCount() + 1 : 1);
            couponRepository.save(coupon);
        });
    }

    private BigDecimal getBookingOriginalPrice(Booking booking) {
        if (booking.getTotalAmount() != null && booking.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            return booking.getTotalAmount();
        }
        if (booking.getTotalPrice() != null && booking.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
            return booking.getTotalPrice();
        }
        if (booking.getBasePrice() != null && booking.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
            return booking.getBasePrice();
        }
        return BigDecimal.ZERO;
    }

    private CouponValidationResponseDTO buildValidationResponse(boolean valid, String code, BigDecimal orderAmount,
                                                                 BigDecimal discountAmount, BigDecimal finalAmount, String message) {
        return CouponValidationResponseDTO.builder()
                .valid(valid)
                .couponCode(code)
                .orderAmount(orderAmount)
                .originalPrice(orderAmount)
                .discountAmount(discountAmount)
                .discountApplied(discountAmount)
                .finalAmount(finalAmount)
                .finalPayablePrice(finalAmount)
                .message(message)
                .build();
    }

    private CouponDTO mapToDTO(Coupon coupon) {
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minBookingValue(coupon.getMinBookingValue())
                .maxDiscount(coupon.getMaxDiscount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .usageLimit(coupon.getUsageLimit())
                .usageLimitPerUser(coupon.getUsageLimitPerUser())
                .usedCount(coupon.getUsedCount())
                .active(coupon.getActive() != null ? coupon.getActive() : coupon.getIsActive())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    private Coupon mapToEntity(CouponDTO dto) {
        return Coupon.builder()
                .id(dto.getId())
                .code(dto.getCode() != null ? dto.getCode().trim() : null)
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minOrderAmount(dto.getMinBookingValue())
                .maxDiscountAmount(dto.getMaxDiscount())
                .validFrom(dto.getValidFrom())
                .validUntil(dto.getValidUntil())
                .usageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 1000)
                .usageLimitPerUser(dto.getUsageLimitPerUser() != null ? dto.getUsageLimitPerUser() : 1)
                .usedCount(dto.getUsedCount() != null ? dto.getUsedCount() : 0)
                .isActive(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    private BookingResponseDTO mapToBookingResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .customerId(booking.getCustomerId())
                .workerId(booking.getWorkerId())
                .serviceId(booking.getServiceId())
                .serviceName(booking.getServiceName())
                .categoryId(booking.getCategoryId())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .payableAmount(booking.getPayableAmount())
                .addressId(booking.getAddressId())
                .addressSummary(booking.getAddressSummary())
                .customerLat(booking.getCustomerLat())
                .customerLng(booking.getCustomerLng())
                .slotId(booking.getSlotId())
                .scheduledTime(booking.getScheduledTime())
                .couponCode(booking.getCouponCode())
                .notes(booking.getNotes())
                .startPinVerified(booking.getStartPinVerified())
                .cancellationReason(booking.getCancellationReason())
                .cancellationFee(booking.getCancellationFee())
                .refundAmount(booking.getRefundAmount())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
