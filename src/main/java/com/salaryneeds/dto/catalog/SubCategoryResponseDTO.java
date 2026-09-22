package com.salaryneeds.dto.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal amount;
    private BigDecimal basePrice;
    private BigDecimal discount;
    private BigDecimal discountPrice;
    private BigDecimal finalAmount;
    private String imageUrl;
    private String image;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getBasePrice() {
        return this.amount != null ? this.amount : this.basePrice;
    }

    public String getImage() {
        return this.imageUrl != null ? this.imageUrl : this.image;
    }
}
