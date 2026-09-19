package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponRequestDTO {

    @NotBlank(message = "Coupon code is required")
    @com.fasterxml.jackson.annotation.JsonAlias({"couponCode", "coupon_code"})
    private String code;

    @JsonProperty("customer_id")
    @com.fasterxml.jackson.annotation.JsonAlias({"customerId", "customer_id", "userId", "user_id"})
    private String customerId;

    @JsonProperty("booking_id")
    @com.fasterxml.jackson.annotation.JsonAlias({"bookingId", "booking_id"})
    private Long bookingId;

    @JsonProperty("booking_amount")
    @com.fasterxml.jackson.annotation.JsonAlias({"bookingAmount", "booking_amount", "orderAmount", "amount"})
    private java.math.BigDecimal bookingAmount;
}
