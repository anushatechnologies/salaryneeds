package com.salaryneeds.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckPhoneRequest {
    @NotBlank
    private String phone;
}
