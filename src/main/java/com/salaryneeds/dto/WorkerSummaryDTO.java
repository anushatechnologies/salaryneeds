package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSummaryDTO {
    private String id;
    private String name;
    private String avatarUrl;
    private String service;          // e.g. "Electrician"
    private List<String> skills;     // ["Wiring", "AC Repair", "Fan Installation"]
    private BigDecimal ratingAvg;    // 4.85
    private Integer completedJobs;   // 127
    private Integer experienceYears; // 5
    private Boolean verified;        // true (show blue tick badge)
    private String maskedPhone;      // Masked contact number
}
