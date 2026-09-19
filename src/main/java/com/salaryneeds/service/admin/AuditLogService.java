package com.salaryneeds.service.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.AuditLogResponseDTO;
import com.salaryneeds.entity.AuditLog;
import com.salaryneeds.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepo;

    /**
     * Record an audit log entry.
     *
     * @param action      the action performed
     * @param description human-readable explanation
     */
    public void log(String action, String description) {
        AuditLog entry = AuditLog.builder()
                .action(action)
                .description(description)
                .build();
        auditLogRepo.save(entry);
    }

    /**
     * Overload for existing admin services that pass adminId and entity metadata.
     */
    public void log(UUID adminId, String action, String entityType,
                    String entityId, String details) {
        String description = (details != null && !details.trim().isEmpty())
                ? details
                : (action + (entityType != null ? " on " + entityType : "") + (entityId != null ? " #" + entityId : ""));
        log(action, description);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<AuditLogResponseDTO> getAuditLogs(String action,
                                                              String description,
                                                              LocalDateTime from,
                                                              LocalDateTime to,
                                                              Pageable pageable) {
        Page<AuditLog> page = auditLogRepo.findWithFilters(action, description, from, to, pageable);
        return toPageResponse(page);
    }

    // ── Mapping ───────────────────────────────────────────────────────────

    private AuditLogResponseDTO toDTO(AuditLog a) {
        return AuditLogResponseDTO.builder()
                .id(a.getId())
                .action(a.getAction())
                .description(a.getDescription())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private PageResponseDTO<AuditLogResponseDTO> toPageResponse(Page<AuditLog> page) {
        List<AuditLogResponseDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return PageResponseDTO.<AuditLogResponseDTO>builder()
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
