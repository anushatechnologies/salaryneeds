package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRedemptionsResponseDTO {

    @JsonProperty("coupon_id")
    private Long couponId;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("usage_count")
    private Integer usageCount;

    @JsonProperty("usage_limit")
    private Integer usageLimit;

    private PageResponseDTO<CouponRedemptionDTO> redemptions;
}
