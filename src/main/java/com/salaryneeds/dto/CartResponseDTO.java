package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO {

    private Long cartId;
    private String customerId;
    @Builder.Default
    private List<CartItemResponseDTO> items = new ArrayList<>();
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;
    private String status;
}
