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
public class WorkerStatusResponse {
    @Builder.Default
    private Boolean success = true;

    private UUID worker_id;
    private Boolean verified;

    @JsonProperty("account_status")
    @JsonAlias({"account_status", "accountStatus"})
    private String account_status;

    @JsonProperty("can_access_dashboard")
    @JsonAlias({"can_access_dashboard", "canAccessDashboard"})
    private Boolean canAccessDashboard;

    @JsonProperty("is_approved")
    @JsonAlias({"is_approved", "isApproved"})
    private Boolean isApproved;

    @JsonProperty("status_message")
    @JsonAlias({"status_message", "statusMessage"})
    private String statusMessage;

    private String message;

    @JsonProperty("workerId")
    public UUID getWorkerIdCamel() {
        return worker_id;
    }

    @JsonProperty("accountStatus")
    public String getAccountStatusCamel() {
        return account_status;
    }

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
        return statusMessage != null ? statusMessage : message;
    }
}
