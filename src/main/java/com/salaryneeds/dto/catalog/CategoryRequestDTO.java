package com.salaryneeds.dto.catalog;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {

    @JsonAlias({"service_id"})
    private UUID serviceId;

    @NotBlank(message = "Category name must not be blank")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be greater than or equal to 0")
    @JsonAlias({"base_price", "basePrice", "price"})
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @JsonAlias({"discount_value", "discountPercent", "discount_percent"})
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @JsonAlias({"final_amount", "discount_price", "discountPrice"})
    private BigDecimal finalAmount;

    @JsonAlias({"icon_url", "image"})
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @JsonAlias({"display_order"})
    private Integer displayOrder;

    @Builder.Default
    private String status = "ACTIVE";
}
