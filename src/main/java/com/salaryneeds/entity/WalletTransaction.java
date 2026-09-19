package com.salaryneeds.entity;

import com.salaryneeds.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "worker_id", nullable = false, length = 64)
    private String workerId;

    @Column(name = "booking_id", length = 64)
    private String bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
