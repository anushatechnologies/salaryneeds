package com.salaryneeds.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AdminCustomerResponseDTO {

    private UUID    id;
    private String  name;
    private String  email;
    private String  phone;
    private String  accountStatus;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime createdAt;
}
