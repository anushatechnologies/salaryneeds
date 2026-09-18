package com.salaryneeds.entity;

import com.salaryneeds.entity.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "worker_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfile {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 254)
    private String email;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "sub_category_id", length = 64)
    private String subCategoryId;

    @Column(name = "sub_category_name", length = 100)
    private String subCategoryName;

    @Column(length = 100)
    private String trade;

    @Column(name = "skills_csv", length = 1000)
    private String skillsCsv;

    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 0;

    @Column(length = 10)
    private String pincode;

    @Column(length = 100)
    private String city;

    @Column(length = 500)
    private String address;

    @Column(name = "service_areas_csv", length = 1000)
    private String serviceAreasCsv;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "duty_online", nullable = false)
    @Builder.Default
    private Boolean dutyOnline = false;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;

    @Column(name = "rating_avg")
    @Builder.Default
    private Double ratingAvg = 5.0;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "acceptance_rate")
    @Builder.Default
    private Double acceptanceRate = 100.0;

    @Column(name = "completion_rate")
    @Builder.Default
    private Double completionRate = 100.0;

    @Column(length = 50)
    @Builder.Default
    private String tier = "STANDARD";

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 30)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.PENDING_APPROVAL;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public List<String> getSkillsList() {
        if (skillsCsv == null || skillsCsv.isBlank()) return new ArrayList<>();
        return Arrays.asList(skillsCsv.split("\\s*,\\s*"));
    }

    public void setSkillsList(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            this.skillsCsv = "";
        } else {
            this.skillsCsv = String.join(", ", skills);
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
}
