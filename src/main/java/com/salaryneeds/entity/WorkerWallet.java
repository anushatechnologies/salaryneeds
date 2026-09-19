package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "worker_wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerWallet {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "worker_id", nullable = false, unique = true, length = 64)
    private String workerId;

    @Column(name = "earnings_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal earningsBalance = BigDecimal.ZERO;

    @Column(name = "today_earnings", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal todayEarnings = BigDecimal.ZERO;

    @Column(name = "this_week_earnings", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal thisWeekEarnings = BigDecimal.ZERO;

    @Column(name = "this_month_earnings", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal thisMonthEarnings = BigDecimal.ZERO;

    @Column(name = "prepaid_duty_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal prepaidDutyBalance = BigDecimal.valueOf(500.00);

    @Column(name = "withdrawable_earnings", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal withdrawableEarnings = BigDecimal.ZERO;

    @Column(name = "locked_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    @Column(name = "lifetime_earned", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal lifetimeEarned = BigDecimal.ZERO;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "account_last4", length = 10)
    private String accountLast4;

    @Column(length = 30)
    private String ifsc;

    @Column(name = "account_holder", length = 120)
    private String accountHolder;

    @Column(name = "bank_verified", nullable = false)
    @Builder.Default
    private Boolean bankVerified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
