package com.salaryneeds.service.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.ReviewCreateRequestDTO;
import com.salaryneeds.dto.admin.ReviewResponseDTO;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.entity.Review;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.ReviewStatus;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.exception.WorkerNotFoundException;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.ReviewRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReviewService {

    private final ReviewRepository        reviewRepo;
    private final CustomerRepository      customerRepo;
    private final WorkerProfileRepository workerRepo;
    private final AuditLogService         auditLogService;

    @Transactional(readOnly = true)
    public PageResponseDTO<ReviewResponseDTO> getAllReviews(Pageable pageable) {
        Page<Review> page = reviewRepo.findAllByOrderByCreatedAtDesc(pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ReviewResponseDTO> getByStatus(ReviewStatus status, Pageable pageable) {
        Page<Review> page = reviewRepo.findAllByStatusOrderByCreatedAtDesc(status, pageable);
        return toPageResponse(page);
    }

    public ReviewResponseDTO create(ReviewCreateRequestDTO req, UUID adminId) {
        // Validate customer exists — name is resolved from the record, never a plain string
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found: " + req.getCustomerId()));

        WorkerProfile worker = workerRepo.findById(req.getWorkerId())
                .orElseThrow(() -> new WorkerNotFoundException(
                        "Worker not found: " + req.getWorkerId()));

        Review review = Review.builder()
                .customerId(req.getCustomerId())
                .workerId(req.getWorkerId())
                .content(req.getContent())
                .rating(req.getRating())
                .status(ReviewStatus.PENDING)
                .build();

        Review saved = reviewRepo.save(review);
        auditLogService.log(adminId, "CREATE_REVIEW", "REVIEW",
                saved.getId().toString(), "Created for customer: " + customer.getName());
        return toDTO(saved, customer.getName(), worker.getName());
    }

    public ReviewResponseDTO approve(Long id, UUID adminId) {
        return updateStatus(id, ReviewStatus.APPROVED, adminId, "APPROVE_REVIEW");
    }

    public ReviewResponseDTO reject(Long id, UUID adminId) {
        return updateStatus(id, ReviewStatus.REJECTED, adminId, "REJECT_REVIEW");
    }

    public void delete(Long id, UUID adminId) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
        reviewRepo.delete(review);
        auditLogService.log(adminId, "DELETE_REVIEW", "REVIEW",
                id.toString(), "Review deleted");
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private ReviewResponseDTO updateStatus(Long id, ReviewStatus status,
                                           UUID adminId, String action) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
        review.setStatus(status);
        reviewRepo.save(review);

        String customerName = resolveCustomerName(review);
        String workerName   = resolveWorkerName(review);

        auditLogService.log(adminId, action, "REVIEW", id.toString(),
                "Status changed to " + status);
        return toDTO(review, customerName, workerName);
    }

    private ReviewResponseDTO toDTO(Review review, String customerName, String workerName) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .customerId(review.getCustomerId())
                .customerName(customerName)
                .workerId(review.getWorkerId())
                .workerName(workerName)
                .content(review.getContent())
                .rating(review.getRating())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private String resolveCustomerName(Review review) {
        if (review.getCustomerId() != null) {
            Customer customer = customerRepo.findById(review.getCustomerId()).orElse(null);
            if (customer != null && customer.getName() != null && !customer.getName().trim().isEmpty()) {
                return customer.getName();
            }
        }
        return "Customer #" + review.getId();
    }

    private String resolveWorkerName(Review review) {
        if (review.getWorkerId() != null) {
            WorkerProfile worker = workerRepo.findById(review.getWorkerId()).orElse(null);
            if (worker != null && worker.getName() != null && !worker.getName().trim().isEmpty()) {
                return worker.getName();
            }
        }
        return "Unassigned Worker";
    }

    private PageResponseDTO<ReviewResponseDTO> toPageResponse(Page<Review> page) {
        List<ReviewResponseDTO> content = page.getContent().stream()
                .map(review -> toDTO(review, resolveCustomerName(review), resolveWorkerName(review)))
                .collect(Collectors.toList());

        return PageResponseDTO.<ReviewResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
