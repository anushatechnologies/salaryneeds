package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.salaryneeds.entity.WorkerProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfileDTO {

    private String id;
    private String name;
    private String phone;
    private String email;
    private String trade;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("sub_category_id")
    private String subCategoryId;

    @JsonProperty("sub_category_name")
    private String subCategoryName;

    @JsonProperty("experience_years")
    private Integer experienceYears;

    private String pincode;
    private String city;
    private String address;

    @JsonProperty("service_areas")
    private List<String> serviceAreas;

    private List<String> skills;
    private Boolean verified;

    @JsonProperty("duty_online")
    private Boolean dutyOnline;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("rating_avg")
    private Double ratingAvg;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

    @JsonProperty("acceptance_rate")
    private Double acceptanceRate;

    @JsonProperty("completion_rate")
    private Double completionRate;

    private String tier;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    public static WorkerProfileDTO fromEntity(WorkerProfile p) {
        if (p == null) return null;
        return WorkerProfileDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .trade(p.getTrade())
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .subCategoryId(p.getSubCategoryId())
                .subCategoryName(p.getSubCategoryName())
                .experienceYears(p.getExperienceYears())
                .pincode(p.getPincode())
                .city(p.getCity())
                .address(p.getAddress())
                .serviceAreas(p.getServiceAreasList())
                .skills(p.getSkillsList())
                .verified(p.getVerified())
                .dutyOnline(p.getDutyOnline())
                .accountStatus(p.getAccountStatus() != null ? p.getAccountStatus().name() : null)
                .ratingAvg(p.getRatingAvg())
                .totalReviews(p.getTotalReviews())
                .acceptanceRate(p.getAcceptanceRate())
                .completionRate(p.getCompletionRate())
                .tier(p.getTier())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
