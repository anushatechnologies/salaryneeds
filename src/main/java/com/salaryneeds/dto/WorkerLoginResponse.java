package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("can_access_dashboard")
    @JsonAlias({"can_access_dashboard", "canAccessDashboard"})
    private Boolean canAccessDashboard;

    @JsonProperty("is_approved")
    @JsonAlias({"is_approved", "isApproved"})
    private Boolean isApproved;

    @JsonProperty("status_message")
    @JsonAlias({"status_message", "statusMessage"})
    private String statusMessage;

    @JsonProperty("canAccessDashboard")
    public Boolean getCanAccessDashboardCamel() {
        return canAccessDashboard;
    }

    @JsonProperty("isApproved")
    public Boolean getIsApprovedCamel() {
        return isApproved;
    }

    @JsonProperty("statusMessage")
    public String getStatusMessageCamel() {
        return statusMessage;
    }
}

