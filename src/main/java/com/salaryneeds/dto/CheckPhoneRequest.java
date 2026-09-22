package com.salaryneeds.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckPhoneRequest {
    @com.fasterxml.jackson.annotation.JsonAlias({"phone number", "phoneNumber", "phone_number", "mobile"})
    @NotBlank
    private String phone;
}
