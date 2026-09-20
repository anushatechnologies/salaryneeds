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
import java.time.LocalDateTime;

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

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 60;

    @Column(name = "inclusions", columnDefinition = "TEXT")
    private String inclusions;

    @Column(name = "exclusions", columnDefinition = "TEXT")
    private String exclusions;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.valueOf(4.80);

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.name != null) {
            this.name = CatalogNameNormalizer.toLowerCaseNormalized(this.name);
        }
        if (this.basePrice == null) {
            this.basePrice = BigDecimal.ZERO;
        }
        if (this.durationMinutes == null) {
            this.durationMinutes = 60;
        }
        if (this.ratingAvg == null) {
            this.ratingAvg = BigDecimal.valueOf(4.80);
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}
