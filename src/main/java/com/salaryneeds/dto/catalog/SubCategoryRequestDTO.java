package com.salaryneeds.dto.catalog;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonAlias({"parent_category", "parentCategoryId", "parent_category_id", "categoryId", "category_id"})
    private UUID parentCategory;

    @JsonAlias({"sub_category_name", "subCategory", "name"})
    @NotBlank(message = "Sub-Category name must not be blank")
    @Size(max = 255, message = "Sub-Category name must not exceed 255 characters")
    private String subCategoryName;

    private String description;

    @JsonAlias({"base_price", "basePrice", "price"})
    private BigDecimal amount;

    @JsonAlias({"discount_value", "discountPercent", "discount_percent"})
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @JsonAlias({"final_price", "final_amount", "finalAmount", "discount_price", "discountPrice"})
    private BigDecimal finalPrice;

    @JsonAlias({"upload_image", "image", "image_url", "imageUrl"})
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String uploadImage;

    @Builder.Default
    private String status = "ACTIVE";

    @JsonIgnore
    public UUID getCategoryId() {
        return this.parentCategory;
    }

    public void setCategoryId(UUID categoryId) {
        this.parentCategory = categoryId;
    }

    @JsonIgnore
    public String getName() {
        return this.subCategoryName;
    }

    public void setName(String name) {
        this.subCategoryName = name;
    }

    @JsonIgnore
    public BigDecimal getFinalAmount() {
        return this.finalPrice;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalPrice = finalAmount;
    }

    @JsonIgnore
    public BigDecimal getDiscountPrice() {
        return this.finalPrice;
    }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.finalPrice = discountPrice;
    }

    @JsonIgnore
    public String getImageUrl() {
        return this.uploadImage;
    }

    public void setImageUrl(String imageUrl) {
        this.uploadImage = imageUrl;
    }

    @JsonIgnore
    public BigDecimal getBasePrice() {
        return this.amount != null ? this.amount : BigDecimal.ZERO;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.amount = basePrice;
    }

    public static class SubCategoryRequestDTOBuilder {
        public SubCategoryRequestDTOBuilder categoryId(UUID categoryId) {
            this.parentCategory = categoryId;
            return this;
        }

        public SubCategoryRequestDTOBuilder name(String name) {
            this.subCategoryName = name;
            return this;
        }

        public SubCategoryRequestDTOBuilder imageUrl(String imageUrl) {
            this.uploadImage = imageUrl;
            return this;
        }

        public SubCategoryRequestDTOBuilder finalAmount(BigDecimal finalAmount) {
            this.finalPrice = finalAmount;
            return this;
        }
    }
}



