package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerLoginRequestDTO {

    @NotBlank(message = "Email or mobile number is required")
    @JsonAlias({"username", "phone", "identifier", "emailOrPhone", "mobile"})
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
