package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private UUID categoryId;
    private String categoryName;
    private String service;
    private String skills;
    private Integer experienceYears;
    private String pincode;
    private Boolean verified;
    private BigDecimal ratingAvg;
    private Integer completedJobsCount;
    private Boolean dutyOnline;
    private Double lastLat;
    private Double lastLng;
    private LocalDateTime lastSeenAt;
    private String accountStatus;
}
