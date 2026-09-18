package com.salaryneeds.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private UUID id;
    private UUID serviceId;
    private String serviceName;
    private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private String status;
    private Integer subCategoriesCount;
    private List<SubCategoryResponseDTO> subCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
