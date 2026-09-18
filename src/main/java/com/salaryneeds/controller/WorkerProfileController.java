package com.salaryneeds.controller;

import com.salaryneeds.dto.DutyUpdateRequest;
import com.salaryneeds.dto.WorkerProfileDTO;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.WorkerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker/profile", "/v1/worker/profile"})
@RequiredArgsConstructor
public class WorkerProfileController {

    private final WorkerProfileService workerProfileService;

    @GetMapping("/me")
    public ResponseEntity<WorkerProfileDTO> getMyProfile(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        WorkerProfileDTO profile = workerProfileService.getProfile(workerId);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/duty")
    public ResponseEntity<Map<String, Object>> updateDuty(
            @RequestBody(required = false) DutyUpdateRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Boolean requestedDuty = request != null ? request.getDutyOnline() : null;
        Map<String, Object> response = workerProfileService.toggleDuty(workerId, requestedDuty);
        return ResponseEntity.ok(response);
    }
}
