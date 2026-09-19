package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {

    @Builder.Default
    private boolean success = true;
    private WalletData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WalletData {
        @JsonProperty("earnings_revenue")
        private EarningsRevenue earningsRevenue;

        @JsonProperty("bank_account")
        private BankAccount bankAccount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EarningsRevenue {
        private BigDecimal balance;

        @JsonProperty("today_earnings")
        private BigDecimal todayEarnings;

        @JsonProperty("this_week_earnings")
        private BigDecimal thisWeekEarnings;

        @JsonProperty("this_month_earnings")
        private BigDecimal thisMonthEarnings;

        @JsonProperty("lifetime_earned")
        private BigDecimal lifetimeEarned;

        @JsonProperty("payout_policy")
        @Builder.Default
        private String payoutPolicy = "AUTO_BANK_SETTLEMENT";

        @JsonProperty("next_auto_settlement_at")
        @Builder.Default
        private String nextAutoSettlementAt = "Tonight at 11:59 PM";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankAccount {
        @JsonProperty("bank_name")
        private String bankName;

        @JsonProperty("account_last4")
        private String accountLast4;

        private String ifsc;

        @JsonProperty("account_holder")
        private String accountHolder;

        private Boolean verified;
    }
}
