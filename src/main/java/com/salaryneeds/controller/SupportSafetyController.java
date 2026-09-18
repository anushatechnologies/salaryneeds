package com.salaryneeds.controller;

import com.salaryneeds.dto.SafetySosRequest;
import com.salaryneeds.dto.SupportTicketRequest;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.SupportSafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker", "/v1/worker"})
@RequiredArgsConstructor
public class SupportSafetyController {

    private final SupportSafetyService supportSafetyService;

    @GetMapping("/support/tickets")
    public ResponseEntity<Map<String, Object>> getTickets(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = supportSafetyService.getTickets(workerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/support/tickets")
    public ResponseEntity<Map<String, Object>> createTicket(
            @RequestBody SupportTicketRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = supportSafetyService.createTicket(workerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/safety/sos")
    public ResponseEntity<Map<String, Object>> triggerSos(
            @RequestBody(required = false) SafetySosRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = supportSafetyService.triggerSos(workerId, request);
        return ResponseEntity.ok(response);
    }
}
