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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SERVICES", uniqueConstraints = {
        @UniqueConstraint(name = "uq_services_category_name", columnNames = {"category_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "discount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "final_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "subcategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Variant> variants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BigDecimal getAmount() {
        return this.basePrice;
    }

    public void setAmount(BigDecimal amount) {
        this.basePrice = amount;
    }

    public String getImage() {
        return this.imageUrl;
    }

    public void setImage(String image) {
        this.imageUrl = image;
    }

    public String getStatus() {
        return (this.isActive != null && this.isActive) ? "ACTIVE" : "INACTIVE";
    }

    public void setStatus(String status) {
        if ("INACTIVE".equalsIgnoreCase(status)) {
            this.isActive = false;
        } else {
            this.isActive = true;
        }
    }

    public void calculateFinalAmount() {
        if (this.basePrice == null) {
            this.basePrice = BigDecimal.ZERO;
        }
        if (this.discount == null || this.discount.compareTo(BigDecimal.ZERO) <= 0) {
            if (this.discountPrice != null && this.discountPrice.compareTo(BigDecimal.ZERO) > 0) {
                this.finalAmount = this.discountPrice;
            } else {
                this.finalAmount = this.basePrice;
            }
        } else if (this.discount.compareTo(BigDecimal.valueOf(100)) <= 0) {
            // Percentage discount: finalAmount = amount - (amount * discount / 100)
            BigDecimal discountAmt = this.basePrice.multiply(this.discount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            this.finalAmount = this.basePrice.subtract(discountAmt).max(BigDecimal.ZERO);
            this.discountPrice = this.finalAmount;
        } else {
            this.finalAmount = this.basePrice.subtract(this.discount).max(BigDecimal.ZERO);
            this.discountPrice = this.finalAmount;
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.name != null) {
            this.name = CatalogNameNormalizer.toLowerCaseNormalized(this.name);
        }
        if (this.basePrice == null) {
            this.basePrice = BigDecimal.ZERO;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        calculateFinalAmount();
    }
}
