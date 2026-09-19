package com.salaryneeds.controller;

import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.JobPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker/jobs", "/v1/worker/jobs"})
@RequiredArgsConstructor
public class JobPortalController {

    private final JobPortalService jobPortalService;

    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getJobFeed(
            @RequestParam(name = "tab", required = false) String tab,
            @RequestParam(name = "category", required = false) String category,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = jobPortalService.getJobFeed(workerId, tab, category);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyJob(
            @PathVariable("id") String jobId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = jobPortalService.applyJob(jobId, workerId);
        return ResponseEntity.ok(response);
    }
}
