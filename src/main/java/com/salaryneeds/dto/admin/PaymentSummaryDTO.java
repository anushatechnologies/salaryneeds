package com.salaryneeds.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentSummaryDTO {

    private long       totalBookings;
    private long       completedBookings;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscount;
}
