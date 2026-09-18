package com.salaryneeds.controller;

import com.salaryneeds.dto.CreateReviewRequest;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. POST /api/customer/reviews — Submit a review
    @PostMapping({"/api/customer/reviews", "/customer/reviews"})
    public ResponseEntity<Map<String, Object>> submitCustomerReview(@Valid @RequestBody CreateReviewRequest request) {
        Map<String, Object> response = reviewService.submitReview(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. GET /api/workers/{workerId}/reviews/summary — Get rating summary
    @GetMapping({"/api/workers/{workerId}/reviews/summary", "/workers/{workerId}/reviews/summary", "/worker/reviews/summary"})
    public ResponseEntity<Map<String, Object>> getWorkerReviewsSummary(
            @PathVariable(value = "workerId", required = false) String workerId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String effectiveWorkerId = (workerId != null && !workerId.isBlank()) ? workerId :
                (WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader);
        Map<String, Object> response = reviewService.getWorkerReviewsSummary(effectiveWorkerId);
        return ResponseEntity.ok(response);
    }

    // 3. GET /api/workers/{workerId}/reviews — Get worker reviews (read-only)
    @GetMapping({"/api/workers/{workerId}/reviews", "/workers/{workerId}/reviews", "/worker/reviews", "/v1/worker/reviews"})
    public ResponseEntity<Map<String, Object>> getWorkerReviews(
            @PathVariable(value = "workerId", required = false) String workerId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String effectiveWorkerId = (workerId != null && !workerId.isBlank()) ? workerId :
                (WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader);
        Map<String, Object> response = reviewService.getWorkerReviews(effectiveWorkerId);
        return ResponseEntity.ok(response);
    }

    // 4. GET /admin/workers/{workerId}/reviews — List reviews for a worker
    @GetMapping("/admin/workers/{workerId}/reviews")
    public ResponseEntity<Map<String, Object>> getAdminWorkerReviews(@PathVariable("workerId") String workerId) {
        Map<String, Object> response = reviewService.getWorkerReviews(workerId);
        return ResponseEntity.ok(response);
    }

    // 5. GET /admin/workers/{workerId}/reviews/summary — Get overall rating card
    @GetMapping("/admin/workers/{workerId}/reviews/summary")
    public ResponseEntity<Map<String, Object>> getAdminWorkerReviewsSummary(@PathVariable("workerId") String workerId) {
        Map<String, Object> response = reviewService.getWorkerReviewsSummary(workerId);
        return ResponseEntity.ok(response);
    }

    // 6. DELETE /admin/reviews/{id} — Delete a review
    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable("id") String id) {
        Map<String, Object> response = reviewService.deleteReview(id);
        return ResponseEntity.ok(response);
    }
}

