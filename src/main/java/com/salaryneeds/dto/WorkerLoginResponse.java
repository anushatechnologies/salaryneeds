package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerLoginResponse {
    @Builder.Default
    private Boolean success = true;
    private String message;
    private UUID worker_id;
    private String name;
    private String phone;
    private String email;
    private Boolean verified;
    private String account_status;
    private String token;
}
