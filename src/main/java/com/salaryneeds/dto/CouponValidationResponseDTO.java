package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResponseDTO {

    private Boolean valid;
    private String couponCode;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal orderAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String message;
}
