package com.salaryneeds.controller;

import com.salaryneeds.dto.DutyUpdateRequest;
import com.salaryneeds.dto.LocationHeartbeatRequest;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.DutyLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker", "/v1/worker"})
@RequiredArgsConstructor
public class DutyLocationController {

    private final DutyLocationService dutyLocationService;

    @RequestMapping(value = {"/duty/toggle", "/duty"}, method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> toggleDuty(
            @RequestBody(required = false) DutyUpdateRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Boolean requestedDuty = request != null ? request.isOnline() : true;
        Map<String, Object> response = dutyLocationService.toggleDuty(workerId, requestedDuty);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/location/heartbeat")
    public ResponseEntity<Map<String, Object>> ping(
            @RequestBody(required = false) Map<String, Object> request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("recorded_at", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
