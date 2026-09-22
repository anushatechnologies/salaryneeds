package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallResponseDTO {
    private String callSessionId;
    private Long bookingId;
    private String callerType;        // "CUSTOMER" or "WORKER"
    private String status;            // "CONNECTING", "BRIDGED", "FAILED"
    private String maskedDisplayNumber; // "+91 80 47*** 10" (virtual bridge number)
    private String message;
    private Integer estimatedWaitSeconds;
    private LocalDateTime initiatedAt;
}
