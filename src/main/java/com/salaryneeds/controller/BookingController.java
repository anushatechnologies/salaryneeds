package com.salaryneeds.controller;

import com.salaryneeds.dto.*;
import com.salaryneeds.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/slots")
    public ResponseEntity<List<SlotResponseDTO>> getAvailableSlots(
            @RequestParam(value = "service_id", required = false) Long serviceId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(bookingService.getAvailableSlots(serviceId, date));
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @Valid @RequestBody BookingCreateRequestDTO request
    ) {
        BookingResponseDTO response = bookingService.createBooking(request, customerIdHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<BookingResponseDTO>> getBookings(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @RequestParam(value = "status", required = false) String statusFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String effectiveCustomerId = (customerIdHeader != null && !customerIdHeader.isBlank())
                ? customerIdHeader.trim()
                : customerIdParam;

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponseDTO<BookingResponseDTO> bookings = bookingService.getBookings(effectiveCustomerId, statusFilter, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader
    ) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId, customerIdHeader));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<CancelBookingResponseDTO> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @Valid @RequestBody BookingCancelRequestDTO request
    ) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, request, customerIdHeader));
    }

    @PostMapping("/{bookingId}/regenerate-pin")
    public ResponseEntity<BookingResponseDTO> regeneratePin(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader
    ) {
        return ResponseEntity.ok(bookingService.regenerateStartPin(bookingId, customerIdHeader));
    }

    // Lifecycle transition endpoint (For Worker / System integration & Postman testing)
    @PostMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponseDTO> updateBookingStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingStatusUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, request.getStatus(), request.getWorkerId()));
    }

    // Verification endpoint for Worker to input customer's PIN
    @PostMapping("/{bookingId}/verify-pin")
    public ResponseEntity<BookingResponseDTO> verifyPin(
            @PathVariable Long bookingId,
            @Valid @RequestBody VerifyPinRequestDTO request
    ) {
        return ResponseEntity.ok(bookingService.verifyPin(bookingId, request.getPin()));
    }

    // Worker Action: Start Travel towards customer location
    @PostMapping("/{bookingId}/start-travel")
    public ResponseEntity<WorkerActionResponseDTO> startTravel(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank()) ? workerIdHeader.trim() : workerIdParam;
        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }
        return ResponseEntity.ok(bookingService.startTravel(bookingId, effectiveWorkerId));
    }

    // Worker Action: Arrived at customer location
    @PostMapping("/{bookingId}/arrived")
    public ResponseEntity<WorkerActionResponseDTO> workerArrived(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank()) ? workerIdHeader.trim() : workerIdParam;
        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }
        return ResponseEntity.ok(bookingService.workerArrived(bookingId, effectiveWorkerId));
    }

    // Worker Action: Verify customer 4-digit start PIN to transition to IN_PROGRESS
    @PostMapping("/{bookingId}/verify-start-pin")
    public ResponseEntity<WorkerActionResponseDTO> verifyStartPin(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam,
            @Valid @RequestBody VerifyPinRequestDTO request
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank()) ? workerIdHeader.trim() : workerIdParam;
        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }
        return ResponseEntity.ok(bookingService.verifyStartPin(bookingId, effectiveWorkerId, request.getPin()));
    }

    // Worker Action: Complete service
    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<WorkerActionResponseDTO> completeService(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank()) ? workerIdHeader.trim() : workerIdParam;
        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }
        return ResponseEntity.ok(bookingService.completeService(bookingId, effectiveWorkerId));
    }

    // Telemetry: Worker sends real-time GPS coordinates during trip
    @PostMapping("/{bookingId}/worker-location")
    public ResponseEntity<WorkerLocationResponseDTO> updateWorkerLocation(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam,
            @Valid @RequestBody WorkerLocationRequestDTO request
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank()) ? workerIdHeader.trim() : workerIdParam;
        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }
        return ResponseEntity.ok(bookingService.updateWorkerLocation(bookingId, effectiveWorkerId, request));
    }

    // Telemetry: Customer / Web / Mobile fetches latest known worker location
    @GetMapping("/{bookingId}/worker-location")
    public ResponseEntity<WorkerLocationResponseDTO> getLatestWorkerLocation(
            @PathVariable Long bookingId
    ) {
        WorkerLocationResponseDTO location = bookingService.getLatestWorkerLocation(bookingId);
        if (location == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(location);
    }
}
