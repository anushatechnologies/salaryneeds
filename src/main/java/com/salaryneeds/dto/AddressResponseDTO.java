package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {

    private UUID id;
    private UUID customerId;
    private String label;
    private String house;
    private String street;
    private String addressLine;
    private String city;
    private String pincode;
    private Double lat;
    private Double lng;
    private Boolean isDefault;
    private String formattedAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
