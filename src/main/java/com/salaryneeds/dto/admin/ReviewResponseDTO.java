package com.salaryneeds.dto.admin;

import com.salaryneeds.entity.enums.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Review response as shown in the admin UI table:
 *   CUSTOMER | WORKER | REVIEW | RATING | STATUS | DATE | ACTION
 *
 * customerName and workerName are resolved from UUIDs so the UI always shows a real name.
 */
@Getter
@Builder
public class ReviewResponseDTO {

    private Long         id;
    private UUID         customerId;
    private String       customerName;   // resolved — never null
    private UUID         workerId;
    private String       workerName;     // resolved — never null
    private String       content;
    private Integer      rating;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
