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
public class WorkerSignupResponse {
    private UUID worker_id;
    private String message;
    private String aadharNumber;
    private String panNumber;
    private String aadharUrl;
    private String panUrl;

    @JsonProperty("account_status")
    @JsonAlias({"account_status", "accountStatus"})
    @Builder.Default
    private String accountStatus = "PENDING_APPROVAL";

    @JsonProperty("can_access_dashboard")
    @JsonAlias({"can_access_dashboard", "canAccessDashboard"})
    @Builder.Default
    private Boolean canAccessDashboard = false;

    @JsonProperty("is_approved")
    @JsonAlias({"is_approved", "isApproved"})
    @Builder.Default
    private Boolean isApproved = false;

    @JsonProperty("status_message")
    @JsonAlias({"status_message", "statusMessage"})
    @Builder.Default
    private String statusMessage = "Your documents are under review by the admin team. You will be able to access the dashboard once approved.";

    @JsonProperty("accountStatus")
    public String getAccountStatusCamel() {
        return accountStatus;
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
        return statusMessage;
    }
}

