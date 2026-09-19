package com.salaryneeds.controller;

import com.salaryneeds.dto.LocationUpdateRequest;
import com.salaryneeds.dto.UpdateAvailabilityRequest;
import com.salaryneeds.dto.UpdateProfileRequest;
import com.salaryneeds.dto.WorkerStatusResponse;
import com.salaryneeds.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/worker")
public class WorkerProfileController {

    @Autowired
    private WorkerService workerService;


    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request, @RequestHeader("X-Worker-ID") UUID workerId) {
        workerService.updateProfile(workerId, request);
        return ResponseEntity.ok(Map.of("updated", true));
    }

    @GetMapping("/status")
    public ResponseEntity<WorkerStatusResponse> getStatus(@RequestHeader("X-Worker-ID") UUID workerId) {
        WorkerStatusResponse response = workerService.getStatus(workerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody UpdateAvailabilityRequest request, @RequestHeader("X-Worker-ID") UUID workerId) {
        // Implementation for setting online/offline status (would need an online field in WorkerProfile)
        return ResponseEntity.ok(Map.of("online", request.getOnline()));
    }

    @PostMapping("/location")
    public ResponseEntity<?> updateLocation(@RequestBody LocationUpdateRequest request, @RequestHeader("X-Worker-ID") UUID workerId) {
        workerService.updateLocation(workerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("recorded_at", LocalDateTime.now()));
    }
    
    @GetMapping("/earnings")
    public ResponseEntity<?> getEarnings(@RequestHeader("X-Worker-ID") UUID workerId) {
        // Stub implementation
        return ResponseEntity.ok(Map.of("total_earnings", 12500, "completed_jobs", 25));
    }
}
