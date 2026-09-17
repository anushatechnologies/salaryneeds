package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
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
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String accountStatus;
    private List<AddressResponseDTO> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
