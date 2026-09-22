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
public class WorkerLocationResponseDTO {

    private Long bookingId;
    private String workerId;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private Double distanceKm;
    private Integer etaMinutes;
    private String etaText;
    private LocalDateTime timestamp;
}
