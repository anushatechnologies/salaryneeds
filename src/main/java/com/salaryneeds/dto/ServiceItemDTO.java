package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceItemDTO {

    private Long id;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private Integer durationMinutes;
    private String inclusions;
    private String exclusions;
    private String imageUrl;
    private BigDecimal ratingAvg;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
