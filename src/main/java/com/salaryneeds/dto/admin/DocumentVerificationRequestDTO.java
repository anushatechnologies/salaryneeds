package com.salaryneeds.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentVerificationRequestDTO {

    /** APPROVED or REJECTED */
    @NotBlank(message = "status is required")
    @Pattern(regexp = "APPROVED|REJECTED", message = "status must be APPROVED or REJECTED")
    private String status;

    private String remarks;
}
