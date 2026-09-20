package com.salaryneeds.dto.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariantResponseDTO {

    private Long id;

    @JsonProperty("parentCategory")
    private String parentCategory;

    @JsonProperty("parentCategoryId")
    private UUID parentCategoryId;

    private UUID categoryId;
    private String categoryName;

    @JsonProperty("parentSubCategory")
    private String parentSubCategory;

    @JsonProperty("parentSubCategoryId")
    private Long parentSubCategoryId;

    private Long subcategoryId;
    private String subcategoryName;

    @JsonProperty("variantName")
    private String variantName;

    private String name;
    private String description;

    @JsonProperty("basePrice")
    private BigDecimal basePrice;

    private BigDecimal amount;
    private BigDecimal discount;

    @JsonProperty("finalPrice")
    private BigDecimal finalPrice;

    private BigDecimal finalAmount;

    @JsonProperty("displayOrder")
    private Integer displayOrder;

    @JsonProperty("variantImage")
    private String variantImage;

    private String imageUrl;
    private String image;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

