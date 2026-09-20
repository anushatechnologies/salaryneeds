package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.BookingResponseDTO;
import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.service.admin.AdminBookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final AdminBookingService bookingService;

    /** GET /api/admin/bookings?status=PENDING&page=0&size=20 */
    @GetMapping
    public ResponseEntity<PageResponseDTO<BookingResponseDTO>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getAllBookings(status, pageable));
    }

    /** GET /api/admin/bookings/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /** PATCH /api/admin/bookings/{id}/status — Manual status override */
    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponseDTO> overrideStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        return ResponseEntity.ok(bookingService.overrideStatus(id, body.get("status"), adminId));
    }
}
