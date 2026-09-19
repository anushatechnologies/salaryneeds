package com.salaryneeds.controller;

import com.salaryneeds.dto.DocumentConfirmRequest;
import com.salaryneeds.dto.UploadUrlRequest;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker/documents", "/v1/worker/documents"})
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload-url")
    public ResponseEntity<Map<String, String>> getUploadUrl(
            @Valid @RequestBody UploadUrlRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, String> response = documentService.generateUploadUrl(workerId != null ? workerId : "w-anonymous", request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"", "/confirm"})
    public ResponseEntity<Map<String, Object>> confirmDocument(
            @Valid @RequestBody DocumentConfirmRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = documentService.confirmDocument(workerId != null ? workerId : "w-anonymous", request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
