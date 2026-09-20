package com.salaryneeds.controller;

import com.salaryneeds.dto.AdminWorkerDocumentDTO;
import com.salaryneeds.service.AdminWorkerDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/workers/documents")
@RequiredArgsConstructor
public class AdminWorkerDocumentController {

    private final AdminWorkerDocumentService adminWorkerDocumentService;

    @GetMapping
    public ResponseEntity<List<AdminWorkerDocumentDTO>> listDocuments() {
        List<AdminWorkerDocumentDTO> documents = adminWorkerDocumentService.listDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminWorkerDocumentDTO> getDocumentById(@PathVariable("id") String id) {
        AdminWorkerDocumentDTO document = adminWorkerDocumentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdminWorkerDocumentDTO> approveDocument(@PathVariable("id") String id) {
        AdminWorkerDocumentDTO approved = adminWorkerDocumentService.approveDocument(id);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AdminWorkerDocumentDTO> rejectDocument(@PathVariable("id") String id) {
        AdminWorkerDocumentDTO rejected = adminWorkerDocumentService.rejectDocument(id);
        return ResponseEntity.ok(rejected);
    }
}
