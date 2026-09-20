package com.salaryneeds.dto.catalog;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VariantRequestDTO {

    @JsonAlias({"parent_category", "parentCategoryId", "parent_category_id", "categoryId", "category_id"})
    private UUID parentCategory;

    @JsonAlias({"parent_subcategory", "parentSubCategory", "parent_subcategory_id", "parentSubCategoryId", "subcategory_id", "subCategoryId"})
    private Long parentSubCategory;

    @JsonAlias({"variant_name", "variantName", "name"})
    @NotBlank(message = "Variant name must not be blank")
    @Size(max = 255, message = "Variant name must not exceed 255 characters")
    private String variantName;

    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be greater than or equal to 0")
    @JsonAlias({"base_price", "basePrice", "price", "amount"})
    private BigDecimal basePrice;

    @JsonAlias({"discount_value", "discountPercent", "discount_percent"})
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @JsonAlias({"final_price", "finalPrice", "final_amount", "discount_price", "discountPrice"})
    private BigDecimal finalPrice;

    @JsonAlias({"display_order", "displayOrder"})
    @Builder.Default
    private Integer displayOrder = 0;

    @JsonAlias({"variant_image", "variantImage", "upload_image", "uploadImage", "image", "image_url", "imageUrl"})
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String variantImage;

    @Builder.Default
    private String status = "ACTIVE";

    @JsonIgnore
    public Long getSubcategoryId() {
        return this.parentSubCategory;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.parentSubCategory = subcategoryId;
    }

    @JsonIgnore
    public String getName() {
        return this.variantName;
    }

    public void setName(String name) {
        this.variantName = name;
    }

    @JsonIgnore
    public BigDecimal getAmount() {
        return this.basePrice;
    }

    public void setAmount(BigDecimal amount) {
        this.basePrice = amount;
    }

    @JsonIgnore
    public BigDecimal getFinalAmount() {
        return this.finalPrice;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalPrice = finalAmount;
    }

    @JsonIgnore
    public String getImageUrl() {
        return this.variantImage;
    }

    public void setImageUrl(String imageUrl) {
        this.variantImage = imageUrl;
    }

    public static class VariantRequestDTOBuilder {
        public VariantRequestDTOBuilder subcategoryId(Long subcategoryId) {
            this.parentSubCategory = subcategoryId;
            return this;
        }

        public VariantRequestDTOBuilder name(String name) {
            this.variantName = name;
            return this;
        }

        public VariantRequestDTOBuilder amount(BigDecimal amount) {
            this.basePrice = amount;
            return this;
        }

        public VariantRequestDTOBuilder finalAmount(BigDecimal finalAmount) {
            this.finalPrice = finalAmount;
            return this;
        }

        public VariantRequestDTOBuilder imageUrl(String imageUrl) {
            this.variantImage = imageUrl;
            return this;
        }
    }
}

