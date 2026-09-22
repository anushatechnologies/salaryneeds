package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.salaryneeds.entity.Coupon;
import com.salaryneeds.entity.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDTO {

    private Long id;

    @NotBlank(message = "Code is required")
    private String code;

    private String description;

    @NotNull(message = "Discount type is required")
    @JsonProperty("discount_type")
    @JsonAlias({"discountType", "discount_type"})
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @JsonProperty("discount_value")
    @JsonAlias({"discountValue", "discount_value"})
    private BigDecimal discountValue;

    @JsonProperty("min_booking_value")
    @JsonAlias({"minBookingValue", "min_booking_value", "minimumOrderAmount", "minimum_order_amount"})
    private BigDecimal minBookingValue;

    @JsonProperty("max_discount")
    @JsonAlias({"maxDiscount", "max_discount", "maximumDiscountAmount", "maximum_discount_amount"})
    private BigDecimal maxDiscount;

    @JsonProperty("valid_from")
    @JsonAlias({"validFrom", "valid_from", "startDate", "start_date"})
    private LocalDateTime validFrom;

    @JsonProperty("valid_until")
    @JsonAlias({"validUntil", "valid_until", "expiryDate", "expiry_date", "endDate", "end_date"})
    private LocalDateTime validUntil;

    @JsonProperty("usage_limit")
    @JsonAlias({"usageLimit", "usage_limit"})
    private Integer usageLimit;

    @JsonProperty("usage_limit_per_user")
    @JsonAlias({"usageLimitPerUser", "usage_limit_per_user", "userLimit", "user_limit"})
    private Integer usageLimitPerUser;

    @JsonProperty("used_count")
    @JsonAlias({"usedCount", "used_count"})
    private Integer usedCount;

    @JsonProperty("active")
    @JsonAlias({"active", "isActive", "is_active", "isAccepted", "is_accepted", "accepted"})
    private Boolean active;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("discountType")
    public DiscountType getDiscountTypeCamel() {
        return discountType;
    }

    @JsonProperty("discountValue")
    public BigDecimal getDiscountValueCamel() {
        return discountValue;
    }

    @JsonProperty("isActive")
    public Boolean getIsActive() {
        return active != null ? active : true;
    }

    public void setIsActive(Boolean isActive) {
        this.active = isActive;
    }

    @JsonProperty("isAccepted")
    public Boolean getIsAccepted() {
        return getIsActive();
    }

    public void setIsAccepted(Boolean isAccepted) {
        this.active = isAccepted;
    }

    public static CouponDTO fromEntity(Coupon coupon) {
        if (coupon == null) return null;
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
}
