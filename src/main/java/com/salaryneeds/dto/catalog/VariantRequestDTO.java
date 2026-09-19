package com.salaryneeds.dto.catalog;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantRequestDTO {

    @JsonAlias({"subcategory_id", "subCategoryId"})
    private Long subcategoryId;

    @NotBlank(message = "Variant name must not be blank")
    @Size(max = 255, message = "Variant name must not exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be greater than or equal to 0")
    @JsonAlias({"base_price", "basePrice", "price"})
    private BigDecimal amount;

    @JsonAlias({"discount_value", "discountPercent", "discount_percent"})
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @JsonAlias({"final_amount", "discount_price", "discountPrice"})
    private BigDecimal finalAmount;

    @JsonAlias({"image", "image_url"})
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Builder.Default
    private String status = "ACTIVE";
}
