package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRedemptionDTO {

    private UUID id;

    @JsonProperty("coupon_id")
    private Long couponId;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("discount_applied")
    private BigDecimal discountApplied;

    @JsonProperty("redeemed_at")
    private LocalDateTime redeemedAt;
}
