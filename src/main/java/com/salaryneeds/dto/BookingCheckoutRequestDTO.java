package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingCheckoutRequestDTO {

    @NotBlank(message = "Address ID is required")
    @JsonAlias({"addressId", "address_id"})
    private String addressId;

    @NotNull(message = "Scheduled date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonAlias({"scheduledDate", "scheduled_date"})
    private LocalDate scheduledDate;

    @JsonAlias({"scheduledTime", "scheduled_time"})
    private String scheduledTime;

    @JsonAlias({"slotId", "slot_id"})
    private String slotId;

    private String notes;

    @JsonAlias({"couponCode", "coupon_code"})
    private String couponCode;

    @JsonAlias({"idempotencyKey", "idempotency_key"})
    private String idempotencyKey;
}
