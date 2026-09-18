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
public class WorkerLoginRequest {
    // Can be passed as username or phone
    private String username;
    private String phone;

    @NotBlank(message = "OTP is required")
    private String otp;

    public String getEffectivePhone() {
        if (username != null && !username.isBlank()) return username.trim();
        if (phone != null && !phone.isBlank()) return phone.trim();
        return "";
    }
}
