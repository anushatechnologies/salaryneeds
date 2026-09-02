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
public class AddressCreateRequestDTO {

    @NotBlank(message = "Label is required")
    private String label;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    @NotBlank(message = "City is required")
    private String city;

    private Boolean isDefault;

}
