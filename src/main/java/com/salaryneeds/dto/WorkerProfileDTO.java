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
    private String categoryId;

    @com.fasterxml.jackson.annotation.JsonAlias({"category_name", "categoryName"})
    private String categoryName;

    @JsonProperty("sub_category_id")
    @com.fasterxml.jackson.annotation.JsonAlias({"sub_category_id", "subCategoryId"})
    private String subCategoryId;

    @JsonProperty("sub_category_name")
    @com.fasterxml.jackson.annotation.JsonAlias({"sub_category_name", "subCategoryName"})
    private String subCategoryName;

    private String service;
    private String trade;

    @com.fasterxml.jackson.annotation.JsonIgnore
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

    @JsonProperty("skills")
    public List<String> getSkills() {
        if (skillsList != null && !skillsList.isEmpty()) {
            return skillsList;
        }
        if (skills != null && !skills.isBlank()) {
            return java.util.Arrays.asList(skills.split("\\s*,\\s*"));
        }
        return java.util.Collections.emptyList();
    }

    public String getSkillsRaw() {
        return skills;
    }

    @JsonProperty("sub_category_id")
    public String getSub_category_id() {
        return subCategoryId;
    }

    @JsonProperty("sub_category_name")
    public String getSub_category_name() {
        return subCategoryName;
    }

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
    public String getCategory_id() {
        return categoryId;
    }

    @JsonProperty("category_name")
    public String getCategory_name() {
        return categoryName;
    }

    @JsonProperty("experience_years")
    public Integer getExperience_years() {
        return experienceYears;
    }

    @JsonProperty("service_areas")
    public List<String> getService_areas() {
        return serviceAreas;
    }

    @JsonProperty("account_status")
    public String getAccount_status() {
        return accountStatus;
    }

    @JsonProperty("created_at")
    public LocalDateTime getCreated_at() {
        return createdAt;
    }

    public static class WorkerProfileDTOBuilder {
        private String categoryId;
        private String skills;
        private List<String> skillsList;

        public WorkerProfileDTOBuilder categoryId(UUID categoryId) {
            this.categoryId = categoryId != null ? categoryId.toString() : null;
            return this;
        }

        public WorkerProfileDTOBuilder categoryId(String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public WorkerProfileDTOBuilder skills(String skills) {
            this.skills = skills;
            if (skills != null && !skills.isBlank()) {
                this.skillsList = java.util.Arrays.asList(skills.split("\\s*,\\s*"));
            }
            return this;
        }

        public WorkerProfileDTOBuilder skills(List<String> skills) {
            this.skillsList = skills;
            if (skills != null) {
                this.skills = String.join(", ", skills);
            }
            return this;
        }
    }

    public static WorkerProfileDTO fromEntity(WorkerProfile p) {
        if (p == null) return null;
        return WorkerProfileDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .categoryId(p.getCategoryId() != null ? p.getCategoryId().toString() : null)
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
