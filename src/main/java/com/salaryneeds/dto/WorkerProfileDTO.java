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

    @JsonProperty("category_id")
    private UUID categoryId;

    @JsonProperty("category_name")
    private String categoryName;

    private String service;
    private String trade;
    private String skills;

    @JsonProperty("skills_list")
    private List<String> skillsList;

    @JsonProperty("experience_years")
    private Integer experienceYears;

    private String pincode;
    private String city;
    private String address;

    @JsonProperty("service_areas")
    private List<String> serviceAreas;

    private Boolean verified;

    @JsonProperty("duty_online")
    private Boolean dutyOnline;

    @JsonProperty("rating_avg")
    private BigDecimal ratingAvg;

    @JsonProperty("completed_jobs_count")
    private Integer completedJobsCount;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

    @JsonProperty("acceptance_rate")
    private Double acceptanceRate;

    @JsonProperty("completion_rate")
    private Double completionRate;

    private String tier;

    @JsonProperty("last_lat")
    private Double lastLat;

    @JsonProperty("last_lng")
    private Double lastLng;

    @JsonProperty("last_seen_at")
    private LocalDateTime lastSeenAt;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

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
