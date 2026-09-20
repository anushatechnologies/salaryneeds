package com.salaryneeds.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequestDTO {

    @NotBlank(message = "Status must not be blank")
    private String status;
}
