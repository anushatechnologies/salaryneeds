package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Audit log entity.
 *
 * audit_logs:
 * - action: the action performed
 * - description: human-readable explanation
 * - createdAt: when the action happened
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The action performed, e.g. APPROVE_DOCUMENT, CREATE_COUPON, BLOCK_WORKER */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /** Human-readable explanation */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** When the action happened */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
