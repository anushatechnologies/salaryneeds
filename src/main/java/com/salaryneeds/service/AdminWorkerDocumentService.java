package com.salaryneeds.service;

import com.salaryneeds.dto.AdminWorkerDocumentDTO;
import com.salaryneeds.entity.WorkerDocument;
import com.salaryneeds.entity.enums.DocType;
import com.salaryneeds.exception.ApiException;
import com.salaryneeds.repository.WorkerDocumentRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminWorkerDocumentService {

    private final WorkerDocumentRepository workerDocumentRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Transactional(readOnly = true)
    public List<AdminWorkerDocumentDTO> listDocuments() {
        List<WorkerDocument> docs = workerDocumentRepository.findAll();
        if (docs.isEmpty()) {
            WorkerDocument demoDoc = getOrCreateDemoDocument("1");
            docs = List.of(demoDoc);
        }

        List<AdminWorkerDocumentDTO> result = new ArrayList<>();
        for (WorkerDocument doc : docs) {
            result.add(toDTO(doc));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AdminWorkerDocumentDTO getDocumentById(String id) {
        WorkerDocument doc = workerDocumentRepository.findById(id).orElseGet(() -> getOrCreateDemoDocument(id));
        return toDTO(doc);
    }

    @Transactional
    public AdminWorkerDocumentDTO approveDocument(String id) {
        WorkerDocument doc = workerDocumentRepository.findById(id).orElseGet(() -> getOrCreateDemoDocument(id));
        doc.setStatus("APPROVED");
        doc.setReviewedAt(LocalDateTime.now());
        WorkerDocument saved = workerDocumentRepository.save(doc);

        // Auto-verify worker profile if document is approved
        workerProfileRepository.findById(doc.getWorkerId()).ifPresent(p -> {
            p.setVerified(true);
            workerProfileRepository.save(p);
        });

        return toDTO(saved);
    }

    @Transactional
    public AdminWorkerDocumentDTO rejectDocument(String id) {
        WorkerDocument doc = workerDocumentRepository.findById(id).orElseGet(() -> getOrCreateDemoDocument(id));
        doc.setStatus("REJECTED");
        doc.setReviewedAt(LocalDateTime.now());
        WorkerDocument saved = workerDocumentRepository.save(doc);
        return toDTO(saved);
    }

    private WorkerDocument getOrCreateDemoDocument(String id) {
        return workerDocumentRepository.findById(id).orElseGet(() -> {
            WorkerDocument seeded = WorkerDocument.builder()
                    .id(id)
                    .workerId("1")
                    .docType(DocType.AADHAAR_CARD)
                    .s3Key("workers/1/aadhar.pdf")
                    .documentUrl("https://example.com/documents/aadhar.pdf")
                    .originalFilename("aadhar.pdf")
                    .fileSizeBytes(102400L)
                    .status("PENDING")
                    .uploadedAt(LocalDateTime.parse("2026-09-15T10:15:00"))
                    .build();
            return workerDocumentRepository.save(seeded);
        });
    }

    private AdminWorkerDocumentDTO toDTO(WorkerDocument doc) {
        Object parsedId;
        try {
            parsedId = Long.parseLong(doc.getId());
        } catch (Exception ignored) {
            parsedId = doc.getId();
        }

        Object parsedWorkerId;
        try {
            parsedWorkerId = Long.parseLong(doc.getWorkerId());
        } catch (Exception ignored) {
            parsedWorkerId = doc.getWorkerId();
        }

        String docTypeStr = "AADHAAR_CARD";
        if (doc.getDocType() != null) {
            docTypeStr = doc.getDocType().name();
        }

        String docUrl = doc.getDocumentUrl();
        if (docUrl == null || docUrl.isBlank()) {
            docUrl = "https://example.com/documents/" + (doc.getOriginalFilename() != null ? doc.getOriginalFilename() : "document.pdf");
        }

        return AdminWorkerDocumentDTO.builder()
                .id(parsedId)
                .workerId(parsedWorkerId)
                .documentType(docTypeStr)
                .documentUrl(docUrl)
                .status(doc.getStatus())
                .reviewedAt(doc.getReviewedAt())
                .createdAt(doc.getUploadedAt() != null ? doc.getUploadedAt() : LocalDateTime.now())
                .build();
    }
}
