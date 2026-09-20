package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponResponseDTO {

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @JsonProperty("discount_applied")
    private BigDecimal discountApplied;

    @JsonProperty("final_payable_price")
    private BigDecimal finalPayablePrice;

    private String message;

    @JsonProperty("success")
    @Builder.Default
    private Boolean success = true;

    public Boolean getSuccess() {
        return success != null ? success : true;
    }

    @JsonProperty("discountAmount")
    public BigDecimal getDiscountAmount() {
        return discountApplied;
    }

    @JsonProperty("finalAmount")
    public BigDecimal getFinalAmount() {
        return finalPayablePrice;
    }
}
