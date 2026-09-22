package com.salaryneeds.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKING_ITEMS", indexes = {
        @Index(name = "idx_booking_items_booking_id", columnList = "booking_id"),
        @Index(name = "idx_booking_items_service_id", columnList = "service_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "service_name", nullable = false, length = 150)
    private String serviceName;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void recalculateTotalPrice() {
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        int qty = (quantity != null && quantity > 0) ? quantity : 0;
        this.totalPrice = price.multiply(BigDecimal.valueOf(qty));
    }
}
