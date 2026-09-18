package com.salaryneeds.dto.catalog;

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
public class SubCategoryRequestDTO {

    private UUID categoryId;

    @NotBlank(message = "Sub-Category name must not be blank")
    @Size(max = 150, message = "Sub-Category name must not exceed 150 characters")
    private String name;

    private String description;

    private BigDecimal basePrice;

    private BigDecimal discountPrice;

    private Integer durationMinutes;

    private String inclusions;

    private String exclusions;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    private String status;
}
