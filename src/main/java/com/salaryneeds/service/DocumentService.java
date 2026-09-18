package com.salaryneeds.service;

import com.salaryneeds.dto.DocumentConfirmRequest;
import com.salaryneeds.dto.UploadUrlRequest;
import com.salaryneeds.entity.WorkerDocument;
import com.salaryneeds.exception.ApiException;
import com.salaryneeds.repository.WorkerDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final WorkerDocumentRepository documentRepository;

    @Value("${aws.s3.bucket-name:salaryneeds-kyc}")
    private String bucketName;

    @Value("${aws.s3.region:ap-south-1}")
    private String region;

    public Map<String, String> generateUploadUrl(String workerId, UploadUrlRequest request) {
        if (request.getDocType() == null) {
            throw new ApiException("ERR_INVALID_DOC_TYPE", "Document type is required. Allowed types: AADHAAR_CARD, PAN_CARD", HttpStatus.BAD_REQUEST);
        }

        String rawFilename = request.getFilename();
        if (rawFilename == null || rawFilename.isBlank()) {
            throw new ApiException("ERR_INVALID_FILENAME", "Filename is required", HttpStatus.BAD_REQUEST);
        }

        String lower = rawFilename.toLowerCase();
        if (!lower.endsWith(".pdf") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png")) {
            throw new ApiException("ERR_UNSUPPORTED_FORMAT", "Only PDF, JPG, JPEG, and PNG files are accepted.", HttpStatus.BAD_REQUEST);
        }

        String filename = rawFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String s3Key = "workers/" + workerId + "/" + filename;
        String uploadUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + s3Key +
                "?AWSAccessKeyId=MOCK_KEY&Signature=MOCK_SIG&Expires=1800";

        Map<String, String> response = new HashMap<>();
        response.put("upload_url", uploadUrl);
        response.put("s3_key", s3Key);
        return response;
    }

    @Transactional
    public Map<String, Object> confirmDocument(String workerId, DocumentConfirmRequest request) {
        if (request.getDocType() == null) {
            throw new ApiException("ERR_INVALID_DOC_TYPE", "Document type is required. Allowed types: AADHAAR_CARD, PAN_CARD", HttpStatus.BAD_REQUEST);
        }

        if (request.getFileSizeBytes() != null && request.getFileSizeBytes() > 5 * 1024 * 1024) {
            throw new ApiException("ERR_FILE_TOO_LARGE", "File size exceeds 5MB limit.", HttpStatus.BAD_REQUEST);
        }

        String docId = "doc-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime now = LocalDateTime.now();

        WorkerDocument doc = WorkerDocument.builder()
                .id(docId)
                .workerId(workerId)
                .docType(request.getDocType())
                .s3Key(request.getS3Key())
                .originalFilename(request.getOriginalFilename())
                .fileSizeBytes(request.getFileSizeBytes())
                .status("PENDING")
                .uploadedAt(now)
                .build();
        WorkerDocument saved = documentRepository.save(doc);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Map<String, Object> docMap = new HashMap<>();
        docMap.put("id", saved.getId());
        docMap.put("doc_type", saved.getDocType().name());
        docMap.put("status", saved.getStatus());
        docMap.put("uploaded_at", now.toString());
        response.put("document", docMap);

        return response;
    }
}
