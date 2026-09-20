package com.salaryneeds.dto.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private String image;
    private String status;
    private Boolean isActive;
    private Integer subCategoriesCount;
    private List<SubCategoryResponseDTO> subCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getImage() {
        return this.imageUrl != null ? this.imageUrl : this.image;
    }
}
