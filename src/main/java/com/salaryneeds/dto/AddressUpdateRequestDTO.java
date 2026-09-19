package com.salaryneeds.dto;

import jakarta.validation.constraints.Pattern;
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

    private String house;

    private String street;

    private String addressLine;

    private String city;

    @Pattern(regexp = "^[0-9]{5,10}$", message = "Pincode must be between 5 and 10 digits")
    private String pincode;

    private Double lat;

    private Double lng;

    private Boolean isDefault;

    public String resolveAddressLine() {
        if (addressLine != null && !addressLine.isBlank()) {
            return addressLine;
        }
        StringBuilder sb = new StringBuilder();
        if (house != null && !house.isBlank()) {
            sb.append(house);
        }
        if (street != null && !street.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(street);
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
