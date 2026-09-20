package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.ReviewCreateRequestDTO;
import com.salaryneeds.dto.admin.ReviewResponseDTO;
import com.salaryneeds.entity.enums.ReviewStatus;
import com.salaryneeds.service.admin.AdminReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService reviewService;

    /**
     * GET /api/admin/reviews?status=PENDING&page=0&size=20
     * status filter matches the "All Statuses" dropdown in the admin UI.
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<ReviewResponseDTO>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (status != null && !status.isBlank()) {
            try {
                ReviewStatus s = ReviewStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(reviewService.getByStatus(s, pageable));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return ResponseEntity.ok(reviewService.getAllReviews(pageable));
    }

    /** POST /api/admin/reviews */
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(
            @Valid @RequestBody ReviewCreateRequestDTO req,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        return ResponseEntity.ok(reviewService.create(req, adminId));
    }

    /** PATCH /api/admin/reviews/{id}/approve */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ReviewResponseDTO> approve(
            @PathVariable Long id,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        return ResponseEntity.ok(reviewService.approve(id, adminId));
    }

    /** PATCH /api/admin/reviews/{id}/reject */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ReviewResponseDTO> reject(
            @PathVariable Long id,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        return ResponseEntity.ok(reviewService.reject(id, adminId));
    }

    /** DELETE /api/admin/reviews/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        reviewService.delete(id, adminId);
        return ResponseEntity.noContent().build();
    }
}
