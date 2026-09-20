package com.salaryneeds.entity;

import com.salaryneeds.entity.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "REVIEWS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The customer who gave this review (mandatory — cannot be null).
     * Resolved to customer name when building response DTOs.
     */
    @Column(name = "customer_id", nullable = false, length = 36)
    private UUID customerId;

    /**
     * The worker being reviewed (mandatory — cannot be null).
     * Resolved to worker name when building response DTOs.
     */
    @Column(name = "worker_id", nullable = false, length = 36)
    private UUID workerId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "rating", nullable = false)
    @Builder.Default
    private Integer rating = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
