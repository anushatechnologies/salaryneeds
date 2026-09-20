package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerActionResponseDTO {

    private Long bookingId;
    private String workerId;
    private BookingStatus status;
    private String message;
    private String navigationUrl;
    private LocalDateTime actionTimestamp;
}
