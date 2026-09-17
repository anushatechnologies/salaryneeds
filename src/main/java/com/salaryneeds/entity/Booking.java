package com.salaryneeds.entity;

import com.salaryneeds.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKINGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "worker_id", length = 36)
    private String workerId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "category_id", length = 50)
    private String categoryId;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "payable_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payableAmount;

    @Column(name = "address_id", length = 36)
    private String addressId;

    @Column(name = "address_summary", length = 500)
    private String addressSummary;

    @Column(name = "slot_id", length = 50)
    private String slotId;

    @Column(name = "scheduled_time", length = 50)
    private String scheduledTime;

    @Column(name = "coupon_code", length = 30)
    private String couponCode;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancellation_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "start_pin_hash")
    private String startPinHash;

    @Column(name = "start_pin_encrypted")
    private String startPinEncrypted;

    @Column(name = "start_pin_verified", nullable = false)
    @Builder.Default
    private Boolean startPinVerified = false;

    @Column(name = "pin_attempts")
    @Builder.Default
    private Integer pinAttempts = 0;

    @Column(name = "pin_expires_at")
    private LocalDateTime pinExpiresAt;

    @Column(name = "service_started_at")
    private LocalDateTime serviceStartedAt;

    @Column(name = "service_completed_at")
    private LocalDateTime serviceCompletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
