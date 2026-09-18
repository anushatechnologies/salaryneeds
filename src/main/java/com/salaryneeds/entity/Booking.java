package com.salaryneeds.entity;

import com.salaryneeds.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "booking_number", length = 64)
    private String bookingNumber;

    @Column(name = "worker_id", length = 64)
    private String workerId;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "customer_name", length = 120)
    private String customerName;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "customer_rating")
    @Builder.Default
    private Double customerRating = 4.8;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "service_title", length = 150)
    private String serviceTitle;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(length = 500)
    private String address;

    @Column(length = 20)
    private String pincode;

    private Double lat;
    private Double lng;

    @Column(name = "scheduled_at", length = 100)
    private String scheduledAt;

    @Column(name = "scheduled_date", length = 30)
    private String scheduledDate;

    @Column(name = "slot_start", length = 30)
    private String slotStart;

    @Column(name = "slot_end", length = 30)
    private String slotEnd;

    @Column(name = "customer_otp", length = 10)
    private String customerOtp;

    @Column(name = "total_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "payout_worker", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal payoutWorker = BigDecimal.ZERO;

    @Column(name = "base_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "extra_parts_total", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal extraPartsTotal = BigDecimal.ZERO;

    @Column(length = 50)
    @Builder.Default
    private String duration = "1.5 Hours";

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "priority_label", length = 50)
    @Builder.Default
    private String priorityLabel = "Normal";

    @Column(name = "customer_avatar", length = 1000)
    private String customerAvatar;

    @Column(name = "sub_category_id", length = 64)
    private String subCategoryId;

    @Column(name = "sub_category_name", length = 100)
    private String subCategoryName;

    @Column(name = "location_area", length = 100)
    private String locationArea;

    @Column(name = "platform_commission", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal platformCommission = BigDecimal.ZERO;

    @Column(name = "before_photos_csv", columnDefinition = "TEXT")
    private String beforePhotosCsv;

    @Column(name = "after_photos_csv", columnDefinition = "TEXT")
    private String afterPhotosCsv;

    @Column(name = "cancelled_by", length = 30)
    private String cancelledBy;

    @Column(name = "is_favourite")
    @Builder.Default
    private Boolean isFavourite = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
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
