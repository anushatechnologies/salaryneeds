package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class BookingCreateRequestDTO {

    private String customerId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private String serviceName;

    private String categoryId;

    @NotNull(message = "Booking date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    private String slotId;

    private String scheduledTime;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    private String addressId;

    private String couponCode;

    private String notes;
}
