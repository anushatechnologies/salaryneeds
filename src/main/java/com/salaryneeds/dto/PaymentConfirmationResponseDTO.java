package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.PaymentMethod;
import com.salaryneeds.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmationResponseDTO {

    private Long bookingId;
    private String workerId;
    private String customerId;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private BigDecimal amountReceived;
    private String transactionReference;
    private String remarks;
    private LocalDateTime confirmedAt;
    private String message;
}
