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
public class ReviewsResponseDTO {

    @Builder.Default
    private boolean success = true;
    private ReviewStats stats;
    private List<ReviewItem> reviews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewStats {
        @JsonProperty("average_rating")
        private Double averageRating;

        @JsonProperty("total_reviews")
        private Integer totalReviews;

        @JsonProperty("five_star_pct")
        private Double fiveStarPct;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewItem {
        private String id;

        @JsonProperty("booking_id")
        private String bookingId;

        @JsonProperty("customer_name")
        private String customerName;

        private Double rating;
        private String comment;
        private String date;
    }
}
