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
public class SubCategoryResponseDTO {

    private Long id;

    @JsonProperty("parentCategory")
    private String parentCategory;

    @JsonProperty("parentCategoryId")
    private UUID parentCategoryId;

    private UUID categoryId;
    private String categoryName;

    @JsonProperty("subCategoryName")
    private String subCategoryName;

    private String name;
    private String description;
    private BigDecimal amount;
    private BigDecimal basePrice;
    private BigDecimal discount;
    private BigDecimal discountPrice;

    @JsonProperty("finalPrice")
    private BigDecimal finalPrice;

    private BigDecimal finalAmount;

    @JsonProperty("uploadImage")
    private String uploadImage;

    private String imageUrl;
    private String image;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


