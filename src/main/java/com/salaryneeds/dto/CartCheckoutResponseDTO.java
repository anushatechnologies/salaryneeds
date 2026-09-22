package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutResponseDTO {

    private Long bookingId;
    @Builder.Default
    private List<Long> bookingIds = new ArrayList<>();
    @Builder.Default
    private List<BookingResponseDTO> bookings = new ArrayList<>();
    private BigDecimal totalAmount;
    private String status;
    private String message;
    @Builder.Default
    private List<BookingItemResponseDTO> items = new ArrayList<>();
}
