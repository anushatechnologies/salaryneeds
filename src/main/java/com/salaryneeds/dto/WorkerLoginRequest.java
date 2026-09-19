package com.salaryneeds.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WorkerLoginRequest {
    @NotBlank
    private String phone;

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "OTP must be exactly 4 digits")
    private String otp;
}
