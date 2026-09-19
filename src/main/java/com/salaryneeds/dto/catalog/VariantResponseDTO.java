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
public class VariantResponseDTO {

    private Long id;
    private Long subcategoryId;
    private String subcategoryName;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private String imageUrl;
    private String image;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getImage() {
        return this.imageUrl != null ? this.imageUrl : this.image;
    }
}
