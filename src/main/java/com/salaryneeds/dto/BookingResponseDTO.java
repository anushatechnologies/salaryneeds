package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.BookingStatus;
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
public class BookingResponseDTO {

    private Long id;
    private String customerId;
    private String workerId;
    private Long serviceId;
    private String serviceName;
    private String categoryId;
    private LocalDate bookingDate;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private String addressId;
    private String addressSummary;
    private Double customerLat;
    private Double customerLng;
    private String navigationUrl;
    private String slotId;
    private String scheduledTime;
    private String couponCode;
    private String notes;
    private String startPin;
    private Boolean startPinVerified;
    private LocalDateTime pinExpiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime enRouteAt;
    private LocalDateTime arrivedAt;
    private String cancellationReason;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private LocalDateTime cancelledAt;
    private WorkerSummaryDTO worker;
    private Double distanceKm;
    private Integer etaMinutes;
    private String etaText;
    private com.salaryneeds.entity.enums.PaymentStatus paymentStatus;
    private com.salaryneeds.entity.enums.PaymentMethod paymentMethod;
    private LocalDateTime paymentConfirmedAt;
    private BigDecimal paymentReceivedAmount;
    private String paymentTransactionRef;
    private String paymentRemarks;
    private LocalDateTime serviceStartedAt;
    private LocalDateTime serviceCompletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
