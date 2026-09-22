package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateCallRequestDTO {
    private String reason; // e.g. "DIRECTIONS", "ARRIVAL_QUERY", "SERVICE_DISCUSSION"
}
