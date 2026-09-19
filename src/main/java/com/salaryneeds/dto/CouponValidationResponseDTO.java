package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("coupon_code")
    private String couponCode;

    private String description;

    @JsonProperty("discount_type")
    private DiscountType discountType;

    @JsonProperty("discount_value")
    private BigDecimal discountValue;

    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    @JsonProperty("discount_applied")
    private BigDecimal discountApplied;

    @JsonProperty("final_amount")
    private BigDecimal finalAmount;

    @JsonProperty("final_payable_price")
    private BigDecimal finalPayablePrice;

    private String message;

    public BigDecimal getOriginalPrice() {
        return originalPrice != null ? originalPrice : orderAmount;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied != null ? discountApplied : discountAmount;
    }

    public BigDecimal getFinalPayablePrice() {
        return finalPayablePrice != null ? finalPayablePrice : finalAmount;
    }

    @JsonProperty("discountAmount")
    public BigDecimal getDiscountAmountCamel() {
        return discountAmount != null ? discountAmount : discountApplied;
    }

    @JsonProperty("finalAmount")
    public BigDecimal getFinalAmountCamel() {
        return finalAmount;
    }

    @JsonProperty("finalPayableAmount")
    public BigDecimal getFinalPayableAmountCamel() {
        return finalPayablePrice != null ? finalPayablePrice : finalAmount;
    }
}
