package com.salaryneeds.dto.admin;

import com.salaryneeds.entity.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponUpdateRequestDTO {

    @Size(max = 30, message = "Coupon code must not exceed 30 characters")
    private String code;

    private String description;

    private DiscountType discountType;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.0", message = "Maximum discount amount cannot be negative")
    private BigDecimal maxDiscountAmount;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    private Boolean isActive;
}
