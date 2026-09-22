package com.salaryneeds.entity;

import com.salaryneeds.entity.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "service", length = 100)
    private String service;

    @Column(name = "skills", length = 255)
    private String skills;

    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 0;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "aadhar_number", length = 30)
    private String aadharNumber;

    @Column(name = "pan_number", length = 30)
    private String panNumber;

    @Column(name = "aadhar_url", length = 1000)
    private String aadharUrl;

    @Column(name = "pan_url", length = 1000)
    private String panUrl;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 30)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "acceptance_rate")
    @Builder.Default
    private Double acceptanceRate = 100.0;

    @Column(name = "completion_rate")
    @Builder.Default
    private Double completionRate = 100.0;

    @Column(name = "tier", length = 50)
    @Builder.Default
    private String tier = "STANDARD";

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Column(name = "service_areas_csv", length = 1000)
    private String serviceAreasCsv;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getTrade() {
        return service;
    }

    public void setTrade(String trade) {
        this.service = trade;
    }

    public UUID getCategoryId() {
        return category != null ? category.getId() : null;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public List<String> getSkillsList() {
        if (skills == null || skills.isBlank()) return new ArrayList<>();
        return Arrays.asList(skills.split("\\s*,\\s*"));
    }

    public void setSkillsList(List<String> skillsList) {
        if (skillsList == null || skillsList.isEmpty()) {
            this.skills = "";
        } else {
            this.skills = String.join(", ", skillsList);
        }
    }

    public List<String> getServiceAreasList() {
        if (serviceAreasCsv == null || serviceAreasCsv.isBlank()) return new ArrayList<>();
        return Arrays.asList(serviceAreasCsv.split("\\s*,\\s*"));
    }

    public void setServiceAreasList(List<String> areas) {
        if (areas == null || areas.isEmpty()) {
            this.serviceAreasCsv = "";
        } else {
            this.serviceAreasCsv = String.join(", ", areas);
        }
    }

    public static class WorkerProfileBuilder {
        public WorkerProfileBuilder trade(String trade) {
            return this.service(trade);
        }
    }
}
