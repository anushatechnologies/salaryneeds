package com.salaryneeds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemUpdateRequestDTO {

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}
