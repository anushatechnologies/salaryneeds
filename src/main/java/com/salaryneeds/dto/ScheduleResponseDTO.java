package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleResponseDTO {

    @Builder.Default
    private boolean success = true;
    private String date;

    @JsonProperty("date_label")
    private String dateLabel;

    private ScheduleSummary summary;
    private List<ScheduleSlot> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleSummary {
        @JsonProperty("booked_visits")
        private Integer bookedVisits;

        @JsonProperty("total_payout")
        private BigDecimal totalPayout;

        @JsonProperty("open_slots")
        private Integer openSlots;

        @JsonProperty("buffer_slots")
        private Integer bufferSlots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScheduleSlot {
        private String id;

        @JsonProperty("time_label")
        private String timeLabel;

        private String session;
        private String status;
        private String notes;

        @JsonProperty("booking_id")
        private String bookingId;

        @JsonProperty("customer_name")
        private String customerName;

        @JsonProperty("customer_phone")
        private String customerPhone;

        @JsonProperty("customer_rating")
        private Double customerRating;

        @JsonProperty("service_title")
        private String serviceTitle;

        @JsonProperty("category_name")
        private String categoryName;

        private String address;
        private BigDecimal payout;
        private String duration;
    }
}
