package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddToCartRequestDTO {

    @NotNull(message = "Service ID is required")
    @JsonAlias({"serviceId", "service_id"})
    private Long serviceId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1;
}
