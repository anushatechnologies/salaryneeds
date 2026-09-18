package com.salaryneeds.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDTO {

    @NotBlank(message = "Service name must not be blank")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    private Integer displayOrder;

    private String status;
}
