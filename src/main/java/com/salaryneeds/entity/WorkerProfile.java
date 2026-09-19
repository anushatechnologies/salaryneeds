package com.salaryneeds.entity;

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
import java.util.UUID;

@Entity
@Table(name = "WORKER_PROFILES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "service")
    private String service;

    @Column(name = "skills")
    private String skills;

    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 0;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.valueOf(5.00);

    @Column(name = "completed_jobs_count")
    @Builder.Default
    private Integer completedJobsCount = 0;

    @Column(name = "duty_online", nullable = false)
    @Builder.Default
    private Boolean dutyOnline = true;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "service_area_lat")
    private Double serviceAreaLat;

    @Column(name = "service_area_lng")
    private Double serviceAreaLng;

    @Column(name = "service_radius_km")
    @Builder.Default
    private Double serviceRadiusKm = 15.0;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "account_status", nullable = false)
    @Builder.Default
    private String accountStatus = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
