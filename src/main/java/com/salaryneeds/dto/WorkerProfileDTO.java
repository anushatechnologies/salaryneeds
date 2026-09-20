package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.salaryneeds.entity.WorkerProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfileDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;

    @com.fasterxml.jackson.annotation.JsonAlias({"category_id", "categoryId"})
    private UUID categoryId;

    @com.fasterxml.jackson.annotation.JsonAlias({"category_name", "categoryName"})
    private String categoryName;

    private String service;
    private String trade;
    private String skills;

    @com.fasterxml.jackson.annotation.JsonAlias({"skills_list", "skillsList"})
    private List<String> skillsList;

    @com.fasterxml.jackson.annotation.JsonAlias({"experience_years", "experienceYears"})
    private Integer experienceYears;

    private String pincode;
    private String city;
    private String address;

    @com.fasterxml.jackson.annotation.JsonAlias({"service_areas", "serviceAreas"})
    private List<String> serviceAreas;

    private Boolean verified;

    @com.fasterxml.jackson.annotation.JsonAlias({"duty_online", "dutyOnline"})
    private Boolean dutyOnline;

    @com.fasterxml.jackson.annotation.JsonAlias({"rating_avg", "ratingAvg"})
    private BigDecimal ratingAvg;

    @com.fasterxml.jackson.annotation.JsonAlias({"completed_jobs_count", "completedJobsCount"})
    private Integer completedJobsCount;

    @com.fasterxml.jackson.annotation.JsonAlias({"total_reviews", "totalReviews"})
    private Integer totalReviews;

    @com.fasterxml.jackson.annotation.JsonAlias({"acceptance_rate", "acceptanceRate"})
    private Double acceptanceRate;

    @com.fasterxml.jackson.annotation.JsonAlias({"completion_rate", "completionRate"})
    private Double completionRate;

    private String tier;

    @com.fasterxml.jackson.annotation.JsonAlias({"last_lat", "lastLat"})
    private Double lastLat;

    @com.fasterxml.jackson.annotation.JsonAlias({"last_lng", "lastLng"})
    private Double lastLng;

    @com.fasterxml.jackson.annotation.JsonAlias({"last_seen_at", "lastSeenAt"})
    private LocalDateTime lastSeenAt;

    @com.fasterxml.jackson.annotation.JsonAlias({"account_status", "accountStatus"})
    private String accountStatus;

    @com.fasterxml.jackson.annotation.JsonAlias({"created_at", "createdAt"})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    @JsonProperty("rating_avg")
    public BigDecimal getRating_avg() {
        return ratingAvg;
    }

    @JsonProperty("duty_online")
    public Boolean getDuty_online() {
        return dutyOnline;
    }

    @JsonProperty("completed_jobs_count")
    public Integer getCompleted_jobs_count() {
        return completedJobsCount;
    }

    @JsonProperty("category_id")
    public UUID getCategory_id() {
        return categoryId;
    }

    public static WorkerProfileDTO fromEntity(WorkerProfile p) {
        if (p == null) return null;
        return WorkerProfileDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .service(p.getService())
                .trade(p.getTrade())
                .skills(p.getSkills())
                .skillsList(p.getSkillsList())
                .experienceYears(p.getExperienceYears())
                .pincode(p.getPincode())
                .city(p.getCity())
                .address(p.getAddress())
                .serviceAreas(p.getServiceAreasList())
                .verified(p.getVerified())
                .dutyOnline(p.getDutyOnline())
                .ratingAvg(p.getRatingAvg())
                .completedJobsCount(p.getCompletedJobsCount())
                .totalReviews(p.getTotalReviews())
                .acceptanceRate(p.getAcceptanceRate())
                .completionRate(p.getCompletionRate())
                .tier(p.getTier())
                .lastLat(p.getLastLat())
                .lastLng(p.getLastLng())
                .lastSeenAt(p.getLastSeenAt())
                .accountStatus(p.getAccountStatus() != null ? p.getAccountStatus().name() : "ACTIVE")
                .createdAt(p.getCreatedAt())
                .build();
    }
}
