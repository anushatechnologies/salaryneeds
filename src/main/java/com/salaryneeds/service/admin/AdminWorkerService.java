package com.salaryneeds.service.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.AccountStatusUpdateRequestDTO;
import com.salaryneeds.dto.admin.AdminWorkerResponseDTO;
import com.salaryneeds.dto.admin.DocumentVerificationRequestDTO;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.exception.WorkerNotFoundException;
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
public class AdminWorkerService {

    private final WorkerProfileRepository workerRepo;
    private final AuditLogService         auditLogService;

    @Transactional(readOnly = true)
    public PageResponseDTO<AdminWorkerResponseDTO> getAllWorkers(Pageable pageable) {
        Page<WorkerProfile> page = workerRepo.findAllByOrderByCreatedAtDesc(pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public AdminWorkerResponseDTO getWorkerById(UUID workerId) {
        WorkerProfile w = workerRepo.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException("Worker not found: " + workerId));
        return toDTO(w);
    }

    /** Block or unblock a worker account. */
    public AdminWorkerResponseDTO updateAccountStatus(UUID workerId,
                                                      AccountStatusUpdateRequestDTO req,
                                                      UUID adminId) {
        WorkerProfile w = workerRepo.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException("Worker not found: " + workerId));
        w.setAccountStatus(req.getAccountStatus());
        workerRepo.save(w);

        auditLogService.log(adminId,
                "SUSPEND_WORKER".equals(req.getAccountStatus()) ? "SUSPEND_WORKER" : "ACTIVATE_WORKER",
                "WORKER", workerId.toString(),
                "Account status changed to " + req.getAccountStatus());
        return toDTO(w);
    }

    /** Workers pending document verification (verified = false). */
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminWorkerResponseDTO> getPendingVerifications(Pageable pageable) {
        Page<WorkerProfile> page = workerRepo.findAllByVerifiedOrderByCreatedAtDesc(false, pageable);
        return toPageResponse(page);
    }

    /** Approve or reject a worker's document. */
    public AdminWorkerResponseDTO verifyDocument(UUID workerId,
                                                 DocumentVerificationRequestDTO req,
                                                 UUID adminId) {
        WorkerProfile w = workerRepo.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException("Worker not found: " + workerId));

        boolean approved = "APPROVED".equalsIgnoreCase(req.getStatus());
        w.setVerified(approved);
        workerRepo.save(w);

        String action = approved ? "APPROVE_DOCUMENT" : "REJECT_DOCUMENT";
        auditLogService.log(adminId, action, "WORKER", workerId.toString(),
                req.getRemarks() != null ? req.getRemarks() : action + " for worker " + workerId);
        return toDTO(w);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    private AdminWorkerResponseDTO toDTO(WorkerProfile w) {
        return AdminWorkerResponseDTO.builder()
                .id(w.getId())
                .name(w.getName())
                .email(w.getEmail())
                .phone(w.getPhone())
                .categoryName(w.getCategory() != null ? w.getCategory().getName() : null)
                .service(w.getService())
                .experienceYears(w.getExperienceYears())
                .pincode(w.getPincode())
                .verified(w.getVerified())
                .ratingAvg(w.getRatingAvg())
                .completedJobsCount(w.getCompletedJobsCount())
                .dutyOnline(w.getDutyOnline())
                .accountStatus(w.getAccountStatus())
                .createdAt(w.getCreatedAt())
                .build();
    }

    private PageResponseDTO<AdminWorkerResponseDTO> toPageResponse(Page<WorkerProfile> page) {
        List<AdminWorkerResponseDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return PageResponseDTO.<AdminWorkerResponseDTO>builder()
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
