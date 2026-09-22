package com.salaryneeds.entity;

import com.salaryneeds.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "booking_number", length = 64)
    private String bookingNumber;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "customer_name", length = 120)
    private String customerName;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "customer_rating")
    @Builder.Default
    private Double customerRating = 4.8;

    @Column(name = "worker_id", length = 36)
    private String workerId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "service_title", length = 150)
    private String serviceTitle;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "sub_category_id", length = 64)
    private String subCategoryId;

    @Column(name = "sub_category_name", length = 100)
    private String subCategoryName;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "payable_amount", precision = 12, scale = 2)
    private BigDecimal payableAmount;

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

    @Column(name = "platform_commission", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal platformCommission = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 30)
    @Builder.Default
    private com.salaryneeds.entity.enums.PaymentStatus paymentStatus = com.salaryneeds.entity.enums.PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private com.salaryneeds.entity.enums.PaymentMethod paymentMethod;

    @Column(name = "payment_confirmed_at")
    private LocalDateTime paymentConfirmedAt;

    @Column(name = "payment_received_amount", precision = 12, scale = 2)
    private BigDecimal paymentReceivedAmount;

    @Column(name = "payment_transaction_ref", length = 100)
    private String paymentTransactionRef;

    @Column(name = "payment_remarks", length = 500)
    private String paymentRemarks;

    @Column(name = "address_id", length = 36)
    private String addressId;

    @Column(name = "address_summary", length = 500)
    private String addressSummary;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "pincode", length = 20)
    private String pincode;

    @Column(name = "location_area", length = 100)
    private String locationArea;

    @Column(name = "customer_lat")
    private Double customerLat;

    @Column(name = "customer_lng")
    private Double customerLng;

    private Double lat;
    private Double lng;

    @Column(name = "slot_id", length = 50)
    private String slotId;

    @Column(name = "scheduled_time", length = 50)
    private String scheduledTime;

    @Column(name = "scheduled_at", length = 100)
    private String scheduledAt;

    @Column(name = "scheduled_date", length = 30)
    private String scheduledDate;

    @Column(name = "slot_start", length = 30)
    private String slotStart;

    @Column(name = "slot_end", length = 30)
    private String slotEnd;

    @Column(name = "duration", length = 50)
    @Builder.Default
    private String duration = "1.5 Hours";

    @Column(name = "priority_label", length = 50)
    @Builder.Default
    private String priorityLabel = "Normal";

    @Column(name = "customer_avatar", length = 1000)
    private String customerAvatar;

    @Column(name = "customer_otp", length = 10)
    private String customerOtp;

    @Column(name = "coupon_code", length = 30)
    private String couponCode;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "description", length = 1000)
    private String description;

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

    @Column(name = "cancelled_by", length = 30)
    private String cancelledBy;

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

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "en_route_at")
    private LocalDateTime enRouteAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "service_started_at")
    private LocalDateTime serviceStartedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "service_completed_at")
    private LocalDateTime serviceCompletedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "before_photos_csv", columnDefinition = "TEXT")
    private String beforePhotosCsv;

    @Column(name = "after_photos_csv", columnDefinition = "TEXT")
    private String afterPhotosCsv;

    @Column(name = "is_favourite")
    @Builder.Default
    private Boolean isFavourite = false;

    @Column(name = "checkout_id", length = 100)
    private String checkoutId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<BookingItem> items = new java.util.ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addItem(BookingItem item) {
        items.add(item);
        item.setBooking(this);
    }

    public void removeItem(BookingItem item) {
        items.remove(item);
        item.setBooking(null);
    }

    public String getBookingNumber() {
        if (bookingNumber != null && !bookingNumber.isBlank()) {
            return bookingNumber;
        }
        return id != null ? String.valueOf(id) : null;
    }

    public String getServiceTitle() {
        if (serviceTitle != null && !serviceTitle.isBlank()) {
            return serviceTitle;
        }
        return serviceName;
    }

    public String getAddress() {
        if (address != null && !address.isBlank()) {
            return address;
        }
        return addressSummary;
    }

    public BigDecimal getTotalPrice() {
        if (totalPrice != null && totalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return totalPrice;
        }
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public BigDecimal getPayoutWorker() {
        if (payoutWorker != null && payoutWorker.compareTo(BigDecimal.ZERO) > 0) {
            return payoutWorker;
        }
        return payableAmount != null ? payableAmount : BigDecimal.ZERO;
    }

    public BigDecimal getBasePrice() {
        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
            return basePrice;
        }
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public String getScheduledAt() {
        if (scheduledAt != null && !scheduledAt.isBlank()) {
            return scheduledAt;
        }
        return scheduledTime;
    }

    public String getDescription() {
        if (description != null && !description.isBlank()) {
            return description;
        }
        return notes;
    }
}
