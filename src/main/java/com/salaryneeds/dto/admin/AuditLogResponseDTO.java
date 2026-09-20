package com.salaryneeds.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Audit log response matching the requested schema:
 * - action: the action performed
 * - description: human-readable explanation
 * - createdAt: when the action happened
 */
@Getter
@Builder
public class AuditLogResponseDTO {

    private Long          id;
    private String        action;
    private String        description;
    private LocalDateTime createdAt;
}
