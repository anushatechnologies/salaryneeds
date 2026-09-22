package com.salaryneeds.controller;

import com.salaryneeds.dto.CallResponseDTO;
import com.salaryneeds.dto.InitiateCallRequestDTO;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.CallBridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/bookings/{bookingId}/call", "/bookings/{bookingId}/call"})
@RequiredArgsConstructor
public class CallController {

    private final CallBridgeService callBridgeService;

    @PostMapping
    public ResponseEntity<CallResponseDTO> initiateCall(
            @PathVariable("bookingId") Long bookingId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestBody(required = false) InitiateCallRequestDTO request) {

        String effectiveWorkerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        InitiateCallRequestDTO req = request != null ? request : new InitiateCallRequestDTO("CONTACT");

        CallResponseDTO response = callBridgeService.initiateMaskedCall(bookingId, customerIdHeader, effectiveWorkerId, req);
        return ResponseEntity.ok(response);
    }
}
