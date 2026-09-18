package com.salaryneeds.controller;

import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker/reviews", "/v1/worker/reviews"})
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReviews(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = reviewService.getWorkerReviews(workerId);
        return ResponseEntity.ok(response);
    }
}
