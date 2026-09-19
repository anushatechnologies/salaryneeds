package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "logo_type", length = 50)
    @Builder.Default
    private String logoType = "building";

    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "salary_range", nullable = false, length = 100)
    private String salaryRange;

    @Column(name = "min_salary", precision = 10, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 10, scale = 2)
    private BigDecimal maxSalary;

    @Column(name = "job_type", length = 50)
    @Builder.Default
    private String jobType = "Full Time";

    @Column(length = 50)
    @Builder.Default
    private String experience = "0-2 Years";

    @Column(length = 30)
    @Builder.Default
    private String status = "Open";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "requirements_csv", columnDefinition = "TEXT")
    private String requirementsCsv;

    @Column(name = "benefits_json", columnDefinition = "TEXT")
    private String benefitsJson;

    @Builder.Default
    private Integer vacancies = 1;

    @Column(length = 100)
    private String education;

    @Column(name = "skills_csv", columnDefinition = "TEXT")
    private String skillsCsv;

    @Column(length = 50)
    private String deadline;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
