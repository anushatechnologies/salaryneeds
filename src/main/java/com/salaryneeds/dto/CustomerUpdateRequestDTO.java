package com.salaryneeds.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUpdateRequestDTO {

    private String name;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    private String defaultAddress;

}
