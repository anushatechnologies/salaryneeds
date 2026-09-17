package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelBookingResponseDTO {

    private Long bookingId;
    private BookingStatus status;
    private String cancellationReason;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private LocalDateTime cancelledAt;
    private String message;
}
