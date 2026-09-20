package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    @Builder.Default
    private boolean success = true;

    private String token;

    private WorkerLoginData worker;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkerLoginData {
        private String id;
        private String name;
        private String phone;
        private String email;
        private String trade;

        @JsonProperty("category_id")
        private String categoryId;

        @JsonProperty("category_name")
        private String categoryName;

        private List<String> skills;

        @JsonProperty("experience_years")
        private Integer experienceYears;

        private String pincode;

        @JsonProperty("service_areas")
        private List<String> serviceAreas;

        private Boolean verified;

        @JsonProperty("rating_avg")
        private Double ratingAvg;

        @JsonProperty("total_reviews")
        private Integer totalReviews;

        @JsonProperty("acceptance_rate")
        private Double acceptanceRate;

        @JsonProperty("completion_rate")
        private Double completionRate;

        private String tier;

        @JsonProperty("duty_online")
        private Boolean dutyOnline;

        @JsonProperty("wallet_summary")
        private WalletSummary walletSummary;

        @JsonProperty("bank_summary")
        private BankSummary bankSummary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WalletSummary {
        @JsonProperty("duty_balance")
        private BigDecimal dutyBalance;

        @JsonProperty("is_active")
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankSummary {
        @JsonProperty("bank_name")
        private String bankName;

        @JsonProperty("account_last4")
        private String accountLast4;

        private Boolean verified;
    }
}
