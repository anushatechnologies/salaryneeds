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

    @PutMapping("/duty/toggle")
    public ResponseEntity<Map<String, Object>> toggleDuty(
            @RequestBody(required = false) DutyUpdateRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Boolean requestedDuty = request != null ? request.getDutyOnline() : null;
        Map<String, Object> response = dutyLocationService.toggleDuty(workerId, requestedDuty);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/location/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(
            @RequestBody LocationHeartbeatRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = dutyLocationService.recordHeartbeat(workerId, request);
        return ResponseEntity.ok(response);
    }
}
