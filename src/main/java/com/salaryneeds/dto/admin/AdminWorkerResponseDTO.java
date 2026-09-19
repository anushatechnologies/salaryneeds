package com.salaryneeds.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AdminWorkerResponseDTO {

    private UUID    id;
    private String  name;
    private String  email;
    private String  phone;
    private String  categoryName;
    private String  service;
    private Integer experienceYears;
    private String  pincode;
    private Boolean verified;
    private BigDecimal ratingAvg;
    private Integer completedJobsCount;
    private Boolean dutyOnline;
    private String  accountStatus;
    private LocalDateTime createdAt;
}
