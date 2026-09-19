package com.salaryneeds.entity;

import com.salaryneeds.util.CatalogNameNormalizer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "VARIANTS", uniqueConstraints = {
        @UniqueConstraint(name = "uq_variants_subcategory_name", columnNames = {"subcategory_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private ServiceItem subcategory;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "discount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getImage() {
        return this.imageUrl;
    }

    public void setImage(String image) {
        this.imageUrl = image;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            this.status = "ACTIVE";
            this.isActive = true;
            return;
        }
        String upper = status.trim().toUpperCase();
        if ("INACTIVE".equalsIgnoreCase(upper)) {
            this.status = "INACTIVE";
            this.isActive = false;
        } else {
            this.status = "ACTIVE";
            this.isActive = true;
        }
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = (isActive != null) ? isActive : true;
        this.status = this.isActive ? "ACTIVE" : "INACTIVE";
    }

    public void calculateFinalAmount() {
        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
        if (this.discount == null || this.discount.compareTo(BigDecimal.ZERO) <= 0) {
            this.finalAmount = this.amount;
        } else if (this.discount.compareTo(BigDecimal.valueOf(100)) <= 0) {
            // Treat as percentage: finalAmount = amount - (amount * discount / 100)
            BigDecimal discountAmt = this.amount.multiply(this.discount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            this.finalAmount = this.amount.subtract(discountAmt).max(BigDecimal.ZERO);
        } else {
            // Flat discount if > 100
            this.finalAmount = this.amount.subtract(this.discount).max(BigDecimal.ZERO);
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.name != null) {
            this.name = CatalogNameNormalizer.toLowerCaseNormalized(this.name);
        }
        if (this.status != null) {
            setStatus(this.status);
        } else if (this.isActive != null) {
            setIsActive(this.isActive);
        } else {
            this.status = "ACTIVE";
            this.isActive = true;
        }
        calculateFinalAmount();
    }
}
