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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CATALOG_SERVICES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

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
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }
}
