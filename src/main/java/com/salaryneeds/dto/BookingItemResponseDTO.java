package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemResponseDTO {

    private Long id;
    private Long serviceId;
    private String serviceName;
    private String categoryId;
    private String categoryName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
