package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotBlank(message = "workerId is required")
    @JsonProperty("worker_id")
    @JsonAlias({"workerId", "worker_id"})
    private String workerId;

    @JsonProperty("booking_id")
    @JsonAlias({"bookingId", "booking_id"})
    private String bookingId;

    @JsonProperty("customer_name")
    @JsonAlias({"customerName", "customer_name"})
    private String customerName;

    @JsonProperty("service_title")
    @JsonAlias({"serviceTitle", "service_title"})
    private String serviceTitle;

    @NotNull(message = "rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private Double rating;

    private String comment;
}
