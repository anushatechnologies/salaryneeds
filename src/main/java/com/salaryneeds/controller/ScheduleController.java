package com.salaryneeds.controller;

import com.salaryneeds.dto.ScheduleResponseDTO;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/worker/schedule", "/v1/worker/schedule"})
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ScheduleResponseDTO> getSchedule(
            @RequestParam(required = false) String date,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        ScheduleResponseDTO schedule = scheduleService.getSchedule(workerId, date);
        return ResponseEntity.ok(schedule);
    }
}
