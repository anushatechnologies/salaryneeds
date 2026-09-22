package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartCheckoutRequestDTO {

    @JsonAlias({"addressId", "address_id"})
    private String addressId;

    @JsonAlias({"address", "full_address", "formatted_address"})
    private String address;

    @JsonAlias({"customerLat", "customer_lat", "lat", "latitude"})
    private Double customerLat;

    @JsonAlias({"customerLng", "customer_lng", "lng", "longitude"})
    private Double customerLng;

    @NotNull(message = "Scheduled date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonAlias({"scheduledDate", "scheduled_date", "bookingDate", "booking_date", "date"})
    private LocalDate scheduledDate;

    @JsonAlias({"scheduledTime", "scheduled_time", "timeSlot", "time_slot", "time"})
    private String scheduledTime;

    @JsonAlias({"slotId", "slot_id"})
    private String slotId;

    private String notes;

    @JsonAlias({"couponCode", "coupon_code"})
    private String couponCode;

    @JsonAlias({"idempotencyKey", "idempotency_key"})
    private String idempotencyKey;
}
