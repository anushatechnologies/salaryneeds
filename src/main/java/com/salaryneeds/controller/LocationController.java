package com.salaryneeds.controller;

import com.salaryneeds.dto.LocationPingRequest;
import com.salaryneeds.entity.LocationLog;
import com.salaryneeds.repository.LocationLogRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.security.WorkerContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/worker/location", "/v1/worker/location"})
@RequiredArgsConstructor
public class LocationController {

    private final LocationLogRepository locationLogRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @PostMapping("/ping")
    public ResponseEntity<Map<String, Object>> pingLocation(
            @Valid @RequestBody LocationPingRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;

        if (workerId != null) {
            LocationLog log = LocationLog.builder()
                    .id("loc-" + java.util.UUID.randomUUID().toString())
                    .workerId(workerId)
                    .bookingId(request.getActiveBookingId())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .speed(request.getSpeed())
                    .heading(request.getHeading())
                    .createdAt(LocalDateTime.now())
                    .build();
            locationLogRepository.save(log);

            workerProfileRepository.findById(workerId).ifPresent(p -> {
                p.setLastLat(request.getLat());
                p.setLastLng(request.getLng());
                workerProfileRepository.save(p);
            });
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("recorded_at", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
