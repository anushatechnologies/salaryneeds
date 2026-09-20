package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.AccountStatusUpdateRequestDTO;
import com.salaryneeds.dto.admin.AdminWorkerResponseDTO;
import com.salaryneeds.dto.admin.DocumentVerificationRequestDTO;
import com.salaryneeds.service.admin.AdminWorkerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/admin/workers", "/admin/workers"})
@RequiredArgsConstructor
public class AdminWorkerController {

    private final AdminWorkerService workerService;

    /** GET /api/admin/workers?page=0&size=20 — All workers */
    @GetMapping
    public ResponseEntity<PageResponseDTO<AdminWorkerResponseDTO>> getAllWorkers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(workerService.getAllWorkers(pageable));
    }

    /** GET /api/admin/workers/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<AdminWorkerResponseDTO> getWorker(@PathVariable UUID id) {
        return ResponseEntity.ok(workerService.getWorkerById(id));
    }

    /** PATCH /api/admin/workers/{id}/status — Block / Unblock */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminWorkerResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AccountStatusUpdateRequestDTO req,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        return ResponseEntity.ok(workerService.updateAccountStatus(id, req, adminId));
    }

    /** GET /api/admin/workers/verifications — Pending document verifications */
    @GetMapping("/verifications")
    public ResponseEntity<PageResponseDTO<AdminWorkerResponseDTO>> getPendingVerifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(workerService.getPendingVerifications(pageable));
    }

    /** PATCH /api/admin/workers/{id}/verify — Approve or Reject document */
    @PatchMapping("/{id}/verify")
    public ResponseEntity<AdminWorkerResponseDTO> verifyDocument(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentVerificationRequestDTO req,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        return ResponseEntity.ok(workerService.verifyDocument(id, req, adminId));
    }
}
