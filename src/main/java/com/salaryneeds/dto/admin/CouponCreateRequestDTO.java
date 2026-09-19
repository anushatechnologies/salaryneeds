package com.salaryneeds.dto.admin;

import com.salaryneeds.entity.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponCreateRequestDTO {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 30, message = "Coupon code must not exceed 30 characters")
    private String code;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Maximum discount amount cannot be negative")
    private BigDecimal maxDiscountAmount;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit = 1000;

    private Boolean isActive = true;
}
