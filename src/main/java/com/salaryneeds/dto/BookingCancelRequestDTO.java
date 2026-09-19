package com.salaryneeds.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCancelRequestDTO {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;

    private String comments;
}
