package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidateRequestDTO {

    @NotBlank(message = "Coupon code is required")
    @com.fasterxml.jackson.annotation.JsonAlias({"couponCode", "coupon_code"})
    private String code;

    @com.fasterxml.jackson.annotation.JsonAlias({"bookingAmount", "booking_amount", "amount", "orderAmount", "order_amount"})
    private BigDecimal orderAmount;

    private BigDecimal price;

    private Long serviceId;

    @JsonProperty("customer_id")
    @com.fasterxml.jackson.annotation.JsonAlias({"customerId", "customer_id", "userId", "user_id"})
    private String customerId;

    @JsonProperty("booking_id")
    @com.fasterxml.jackson.annotation.JsonAlias({"bookingId", "booking_id"})
    private Long bookingId;

    public BigDecimal getOrderAmount() {
        if (orderAmount != null) {
            return orderAmount;
        }
        return price;
    }
}
