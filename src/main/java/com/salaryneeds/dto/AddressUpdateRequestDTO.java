package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressUpdateRequestDTO {

    private String label;

    private String addressLine;

    private String pincode;

    private String city;

    private Boolean isDefault;

}
