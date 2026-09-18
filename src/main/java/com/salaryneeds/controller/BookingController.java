package com.salaryneeds.controller;

import com.salaryneeds.dto.*;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/worker/bookings", "/v1/worker/bookings"})
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping({"/radar", "/nearby-leads"})
    public ResponseEntity<Map<String, Object>> getNearbyLeads(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(name = "radius_km", required = false) Double radiusKm) {
        List<NearbyLeadDTO> leads = bookingService.getNearbyLeads(lat, lng, radiusKm);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", leads.size());
        response.put("leads", leads);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/accept")
    public ResponseEntity<Map<String, Object>> acceptBooking(
            @PathVariable String bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = bookingService.acceptBooking(bookingId, workerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/decline")
    public ResponseEntity<Map<String, Object>> declineBooking(
            @PathVariable String bookingId,
            @RequestBody(required = false) DeclineLeadRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = bookingService.declineBooking(bookingId, workerId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> getBooking(
            @PathVariable String bookingId) {
        Map<String, Object> response = bookingService.getBookingDetails(bookingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingStatusUpdateRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = bookingService.updateBookingStatus(bookingId, workerId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/extra-parts")
    public ResponseEntity<Map<String, Object>> addExtraPart(
            @PathVariable String bookingId,
            @Valid @RequestBody ExtraPartRequest request) {
        Map<String, Object> response = bookingService.addExtraPart(bookingId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{bookingId}/checklist/{itemId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleChecklist(
            @PathVariable String bookingId,
            @PathVariable String itemId) {
        Map<String, Object> response = bookingService.toggleChecklistItem(bookingId, itemId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @PathVariable String bookingId,
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = bookingService.verifyOtpAndComplete(bookingId, workerId, request.getOtp());
        return ResponseEntity.ok(response);
    }
}
