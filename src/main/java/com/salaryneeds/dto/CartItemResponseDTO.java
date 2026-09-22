package com.salaryneeds.dto;

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
public class CartItemResponseDTO {

    private Long id;
    private Long serviceId;
    private String serviceName;
    private String categoryId;
    private String categoryName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
