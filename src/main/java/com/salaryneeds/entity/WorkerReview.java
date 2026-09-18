package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "worker_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerReview {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "worker_id", nullable = false, length = 64)
    private String workerId;

    @Column(name = "booking_id", length = 64)
    private String bookingId;

    @Column(name = "customer_name", length = 120)
    private String customerName;

    @Column(name = "service_title", length = 150)
    private String serviceTitle;

    @Column(nullable = false)
    private Double rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
