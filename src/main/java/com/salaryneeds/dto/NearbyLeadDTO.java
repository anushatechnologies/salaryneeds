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
public class NearbyLeadDTO {

    private String id;

    @JsonProperty("booking_id")
    private String bookingId;

    private String title;
    private String category;

    @JsonProperty("payout_range")
    private String payoutRange;

    @JsonProperty("base_payout")
    private BigDecimal basePayout;

    @JsonProperty("distance_km")
    private Double distanceKm;

    @JsonProperty("time_ago")
    private String timeAgo;

    private Double rating;

    @JsonProperty("rating_count")
    private Integer ratingCount;

    @JsonProperty("priority_label")
    private String priorityLabel;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_phone")
    private String customerPhone;

    private String address;
    private String pincode;
    private String description;

    @JsonProperty("scheduled_at")
    private String scheduledAt;

    private String duration;
}
