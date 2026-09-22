package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerPaymentConfirmationRequestDTO {

    /**
     * True if worker received payment, False if customer didn't pay / dispute.
     */
    @Builder.Default
    private Boolean received = true;

    private PaymentMethod paymentMethod; // CASH, UPI, QR_CODE, ONLINE, WALLET

    private BigDecimal amountReceived;

    private String transactionReference; // UPI UTR or bank reference (optional)

    private String remarks; // Notes or reason if not received
}
