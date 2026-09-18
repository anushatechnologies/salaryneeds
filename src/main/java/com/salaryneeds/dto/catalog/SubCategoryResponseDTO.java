package com.salaryneeds.dto.catalog;

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
public class SubCategoryResponseDTO {

    private Long id;
    private UUID categoryId;
    private String categoryName;
    private UUID serviceId;
    private String serviceName;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private Integer durationMinutes;
    private String inclusions;
    private String exclusions;
    private String imageUrl;
    private BigDecimal ratingAvg;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
