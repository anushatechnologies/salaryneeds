package com.salaryneeds.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLoginRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
