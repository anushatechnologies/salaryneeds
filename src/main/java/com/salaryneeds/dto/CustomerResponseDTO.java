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
public class CustomerResponseDTO {

    private UUID id;

    private String name;

    private String email;

    private String phone;

    private String defaultAddress;

    @JsonProperty("emailVerified")
    private Boolean emailVerified;

    @JsonProperty("phoneVerified")
    private Boolean phoneVerified;

    private String accountStatus;

}
