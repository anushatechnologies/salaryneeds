package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponseDTO {

    private String slotId;
    private String timeRange;
    private String startTime;
    private String endTime;
    private Boolean isAvailable;
    private LocalDateTime cutoffTime;
    private String message;
}
