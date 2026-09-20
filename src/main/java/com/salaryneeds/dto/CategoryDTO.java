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
public class CategoryDTO {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private String iconUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Integer servicesCount;
    @Builder.Default
    private java.util.List<ServiceItemDTO> subCategories = new java.util.ArrayList<>();
    @Builder.Default
    private java.util.List<ServiceItemDTO> services = new java.util.ArrayList<>();
    private LocalDateTime createdAt;
}
