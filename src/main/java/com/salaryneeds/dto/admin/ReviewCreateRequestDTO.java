package com.salaryneeds.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReviewCreateRequestDTO {

    @NotNull(message = "customerId is required")
    private UUID customerId;

    @NotNull(message = "workerId is required")
    private UUID workerId;

    @NotBlank(message = "content is required")
    private String content;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;
}
