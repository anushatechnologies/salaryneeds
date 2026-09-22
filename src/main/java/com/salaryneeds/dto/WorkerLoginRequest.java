package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerLoginRequest {

    @NotBlank(message = "Phone number is required")
    @JsonAlias({"mobile", "phoneNumber", "mobileNumber", "phone_number", "username"})
    private String phone;

    private String username;

    @NotBlank(message = "OTP is required")
    @JsonAlias({"verificationCode", "code"})
    private String otp;

    public void setUsername(String username) {
        this.username = username;
        if (this.phone == null || this.phone.isBlank()) {
            this.phone = username;
        }
    }

    public String getEffectivePhone() {
        if (phone != null && !phone.isBlank()) {
            return phone.trim();
        }
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return null;
    }

    public static class WorkerLoginRequestBuilder {
        public WorkerLoginRequestBuilder username(String username) {
            this.username = username;
            if (this.phone == null || this.phone.isBlank()) {
                this.phone = username;
            }
            return this;
        }
    }
}
