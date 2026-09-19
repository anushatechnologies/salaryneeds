package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTokenRequest {

    @NotBlank(message = "Token is required")
    @JsonAlias({"token", "device_token", "deviceToken"})
    private String token;

    private String platform;

    @JsonProperty("device_name")
    @JsonAlias({"deviceName", "device_name"})
    private String deviceName;
}
