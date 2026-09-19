package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusUpdateRequestDTO {

    @NotNull(message = "Status is required")
    private BookingStatus status;

    private String workerId;

    private String notes;
}
