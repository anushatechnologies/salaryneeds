package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {

    private UUID id;

    private String label;

    private String addressLine;

    private String pincode;

    private String city;

    @JsonProperty("isDefault")
    private Boolean isDefault;

}
