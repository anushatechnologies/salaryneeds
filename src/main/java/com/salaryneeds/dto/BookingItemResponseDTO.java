package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItemResponseDTO {

    private Long bookingItemId;
    private Long bookingId;
    private Long serviceId;
    private String serviceName;
    private String categoryId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
