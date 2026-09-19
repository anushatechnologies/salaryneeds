package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingCreateRequestDTO {

    @JsonAlias({"customerId", "customer_id"})
    private String customerId;

    @NotNull(message = "Service ID is required")
    @JsonAlias({"serviceId", "service_id"})
    private Long serviceId;

    @JsonAlias({"serviceName", "service_name"})
    private String serviceName;

    @JsonAlias({"categoryId", "category_id"})
    private String categoryId;

    @NotNull(message = "Booking date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonAlias({"bookingDate", "booking_date"})
    private LocalDate bookingDate;

    @JsonAlias({"slotId", "slot_id"})
    private String slotId;

    @JsonAlias({"scheduledTime", "scheduled_time"})
    private String scheduledTime;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    @JsonAlias({"totalAmount", "total_amount"})
    private BigDecimal totalAmount;

    @JsonAlias({"addressId", "address_id"})
    private String addressId;

    @JsonAlias({"customerLat", "customer_lat", "lat", "latitude"})
    private Double customerLat;

    @JsonAlias({"customerLng", "customer_lng", "lng", "longitude"})
    private Double customerLng;

    @JsonAlias({"couponCode", "coupon_code"})
    private String couponCode;

    private String notes;
}
