package com.salaryneeds.controller;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerActionResponseDTO;
import com.salaryneeds.entity.enums.BookingOfferStatus;
import com.salaryneeds.service.BookingOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingOfferController {

    private final BookingOfferService bookingOfferService;

    @GetMapping({"/api/worker/offers", "/api/booking-offers"})
    public ResponseEntity<List<BookingOfferResponseDTO>> getWorkerOffers(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam,
            @RequestParam(value = "status", required = false) BookingOfferStatus status
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank())
                ? workerIdHeader.trim()
                : workerIdParam;

        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }

        return ResponseEntity.ok(bookingOfferService.getOffersForWorker(effectiveWorkerId, status));
    }

    @PostMapping({"/api/worker/offers/{offerId}/accept", "/api/booking-offers/{offerId}/accept"})
    public ResponseEntity<WorkerActionResponseDTO> acceptOffer(
            @PathVariable Long offerId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank())
                ? workerIdHeader.trim()
                : workerIdParam;

        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }

        return ResponseEntity.ok(bookingOfferService.acceptOffer(offerId, effectiveWorkerId));
    }

    @PostMapping({"/api/worker/offers/{offerId}/reject", "/api/booking-offers/{offerId}/reject"})
    public ResponseEntity<WorkerActionResponseDTO> rejectOffer(
            @PathVariable Long offerId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestParam(value = "workerId", required = false) String workerIdParam
    ) {
        String effectiveWorkerId = (workerIdHeader != null && !workerIdHeader.isBlank())
                ? workerIdHeader.trim()
                : workerIdParam;

        if (effectiveWorkerId == null || effectiveWorkerId.isBlank()) {
            throw new IllegalArgumentException("Worker ID is required in X-Worker-Id header or workerId query param");
        }

        return ResponseEntity.ok(bookingOfferService.rejectOffer(offerId, effectiveWorkerId));
    }
}
