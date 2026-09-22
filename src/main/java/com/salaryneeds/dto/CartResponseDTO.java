package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private Long id;
    private String customerId;
    private CartStatus status;
    @Builder.Default
    private List<CartItemResponseDTO> items = new ArrayList<>();
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
