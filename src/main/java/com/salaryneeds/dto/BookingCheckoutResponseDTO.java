package com.salaryneeds.dto;

import com.salaryneeds.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCheckoutResponseDTO {

    private boolean success;
    private String message;
    private Long cartId;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private BookingResponseDTO booking;
    @Builder.Default
    private List<BookingResponseDTO> bookings = new ArrayList<>();
}
