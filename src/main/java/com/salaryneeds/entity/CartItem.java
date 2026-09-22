package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CART_ITEMS", indexes = {
        @Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
        @Index(name = "idx_cart_items_service_id", columnList = "service_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "service_name", length = 150)
    private String serviceName;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "image_url")
    private String imageUrl;

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
        if (this.price != null && this.quantity != null) {
            this.totalPrice = this.price.multiply(BigDecimal.valueOf(this.quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
}
