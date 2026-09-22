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
public class CartItemResponseDTO {

    private Long cartItemId;
    private Long serviceId;
    private String serviceName;
    private String categoryId;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
