package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.BookingResponseDTO;
import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.PaymentSummaryDTO;
import com.salaryneeds.service.admin.AdminBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminBookingService bookingService;

    /** GET /api/admin/payments/summary */
    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryDTO> getSummary() {
        return ResponseEntity.ok(bookingService.getPaymentSummary());
    }

    /** GET /api/admin/payments/details?page=0&size=20 — completed bookings */
    @GetMapping("/details")
    public ResponseEntity<PageResponseDTO<BookingResponseDTO>> getDetails(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getPaymentDetails(pageable));
    }
}
