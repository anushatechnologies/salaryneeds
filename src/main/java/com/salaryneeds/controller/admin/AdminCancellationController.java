package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.BookingResponseDTO;
import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.service.admin.AdminBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/admin/cancellations", "/admin/cancellations", "/api/admin/bookings/cancellations", "/admin/bookings/cancellations"})
@RequiredArgsConstructor
public class AdminCancellationController {

    private final AdminBookingService bookingService;

    /** GET /api/admin/cancellations?page=0&size=20 — cancelled bookings with fees & refunds */
    @GetMapping
    public ResponseEntity<PageResponseDTO<BookingResponseDTO>> getCancellationReport(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getCancellationReport(pageable));
    }
}
