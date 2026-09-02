package com.salaryneeds.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class WorkerSignupRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "OTP must be exactly 4 digits")
    private String otp;

    @NotNull
    private UUID category_id;

    @NotBlank
    private String service;

    private String skills;

    private Integer experience_years;

    @NotBlank
    private String pincode;
}
