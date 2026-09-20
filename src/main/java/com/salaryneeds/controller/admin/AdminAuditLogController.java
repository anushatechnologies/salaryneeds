package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.AuditLogResponseDTO;
import com.salaryneeds.service.admin.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping({"/api/admin/audit-logs", "/admin/audit-logs"})
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    /**
     * GET /api/admin/audit-logs
     *   ?action=APPROVE_DOCUMENT          — filter by action keyword (partial match)
     *   &description=...                  — filter by description keyword
     *   &from=2026-09-01                  — date range start (inclusive)
     *   &to=2026-09-30                    — date range end (inclusive)
     *   &page=0&size=20
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<AuditLogResponseDTO>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime fromDt = (from != null) ? from.atStartOfDay()          : null;
        LocalDateTime toDt   = (to   != null) ? to.atTime(23, 59, 59) : null;

        return ResponseEntity.ok(
                auditLogService.getAuditLogs(action, description, fromDt, toDt, pageable));
    }
}
