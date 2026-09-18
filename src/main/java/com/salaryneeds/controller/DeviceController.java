package com.salaryneeds.controller;

import com.salaryneeds.dto.DeviceTokenRequest;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping({"/worker/devices", "/v1/worker/devices"})
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/register-token")
    public ResponseEntity<Map<String, Boolean>> registerToken(
            @Valid @RequestBody DeviceTokenRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        deviceService.registerDeviceToken(workerId != null ? workerId : "w-anonymous", request);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
