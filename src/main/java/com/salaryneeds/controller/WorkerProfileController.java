package com.salaryneeds.controller;

import com.salaryneeds.dto.DutyUpdateRequest;
import com.salaryneeds.dto.WorkerProfileDTO;
import com.salaryneeds.dto.WorkerStatusResponse;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.WorkerProfileService;
import com.salaryneeds.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker/profile", "/v1/worker/profile", "/api/worker/profile", "/api/workers/profile"})
@RequiredArgsConstructor
public class WorkerProfileController {

    private final WorkerProfileService workerProfileService;
    private final WorkerService workerService;

    @GetMapping({"", "/me", "/{workerId}"})
    public ResponseEntity<WorkerProfileDTO> getMyProfile(
            @PathVariable(required = false) String workerId,
            @RequestParam(required = false) String worker_id,
            @RequestParam(required = false) String id,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String effectiveId = workerId;
        if (effectiveId == null || effectiveId.isBlank() || "me".equalsIgnoreCase(effectiveId)) {
            effectiveId = (worker_id != null && !worker_id.isBlank()) ? worker_id : id;
        }
        if (effectiveId == null || effectiveId.isBlank()) {
            effectiveId = workerIdHeader;
        }
        if (effectiveId == null || effectiveId.isBlank()) {
            effectiveId = WorkerContext.getWorkerId();
        }
        WorkerProfileDTO profile = workerProfileService.getProfile(effectiveId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/status")
    public ResponseEntity<WorkerStatusResponse> getMyStatus(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        java.util.UUID uuid = com.salaryneeds.util.UuidUtil.parseUuid(workerId);
        if (uuid == null) {
            return ResponseEntity.badRequest().body(WorkerStatusResponse.builder()
                    .success(false)
                    .message("Worker ID is required to check status")
                    .build());
        }
        return ResponseEntity.ok(workerService.getStatus(uuid));
    }

    @RequestMapping(value = "/duty", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Map<String, Object>> updateDuty(
            @RequestBody(required = false) DutyUpdateRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Boolean requestedDuty = request != null ? request.isOnline() : true;
        Map<String, Object> response = workerProfileService.toggleDuty(workerId, requestedDuty);
        return ResponseEntity.ok(response);
    }
}

