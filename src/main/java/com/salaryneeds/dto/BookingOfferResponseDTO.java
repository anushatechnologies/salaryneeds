package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.BookingOfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingOfferResponseDTO {

    private Long id;
    private Long bookingId;
    private String workerId;
    private String serviceName;
    private String categoryId;
    private String customerArea;
    private Double distanceKm;
    private BigDecimal totalAmount;
    private BigDecimal payableAmount;
    private LocalDate bookingDate;
    private String scheduledTime;
    private String slotId;
    private BookingOfferStatus status;
    private LocalDateTime offeredAt;
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;
}
