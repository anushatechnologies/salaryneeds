package com.salaryneeds.service.storage;

import com.salaryneeds.exception.InvalidFileException;
import com.salaryneeds.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseStorageServiceImpl implements SupabaseStorageService {

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private static final Set<String> ALLOWED_DOC_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp",
            "application/pdf",
            "application/json"
    );

    private final S3Client s3Client;

    @Value("${SUPABASE_CATALOG_BUCKET:${supabase.catalog.bucket:salaryneeds}}")
    private String bucket;

    @Value("${SUPABASE_S3_ENDPOINT:${supabase.s3.endpoint:https://jbgctbgqymrdurkumwes.storage.supabase.co/storage/v1/s3}}")
    private String endpoint;

    @Value("${storage.public.base-url:${STORAGE_PUBLIC_BASE_URL:}}")
    private String publicBaseUrl;

    @Override
    public String uploadCategoryImage(UUID categoryId, MultipartFile file) {
        if (categoryId == null) {
            throw new InvalidFileException("Category ID cannot be null.");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Image file cannot be null or empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the maximum allowed limit of 5 MB.");
        }

        String contentType = file.getContentType();
        validateContentType(contentType);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new StorageException("Failed to read image file data: " + e.getMessage(), e);
        }

        return upload(categoryId, bytes, file.getOriginalFilename(), contentType);
    }

    @Override
    public String uploadCategoryImage(UUID categoryId, byte[] fileBytes, String originalFilename, String contentType) {
        if (categoryId == null) {
            throw new InvalidFileException("Category ID cannot be null.");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            throw new InvalidFileException("Image file cannot be null or empty.");
        }
        if (fileBytes.length > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the maximum allowed limit of 5 MB.");
        }

        validateContentType(contentType);

        return upload(categoryId, fileBytes, originalFilename, contentType);
    }

    private void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase().trim())) {
            throw new InvalidFileException("Invalid file type. Only PNG, JPEG, and WebP images are allowed.");
        }
    }

    private String upload(UUID categoryId, byte[] fileBytes, String originalFilename, String contentType) {
        String extension = determineExtension(contentType, originalFilename);
        String generatedFileName = UUID.randomUUID() + extension;
        String storagePath = "categories/" + categoryId + "/" + generatedFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storagePath)
                    .contentType(contentType.toLowerCase().trim())
                    .contentLength((long) fileBytes.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
            log.info("Successfully uploaded category image to bucket '{}' at path '{}'", bucket, storagePath);
            return storagePath;
        } catch (SdkException e) {
            log.error("Failed to upload category image to Supabase storage bucket '{}' key '{}': {}", bucket, storagePath, e.getMessage(), e);
            throw new StorageException("Failed to upload category image to storage: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading category image: {}", e.getMessage(), e);
            throw new StorageException("Unexpected error during image upload: " + e.getMessage(), e);
        }
    }

    private String determineExtension(String contentType, String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".webp")) {
                return ext;
            }
        }
        return switch (contentType.toLowerCase().trim()) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> ".png";
        };
    }

    @Override
    public String uploadWorkerDocument(String workerId, String docType, MultipartFile file) {
        if (workerId == null || workerId.isBlank()) {
            throw new InvalidFileException("Worker ID cannot be null or empty.");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Document file cannot be null or empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the maximum allowed limit of 5 MB.");
        }

        String contentType = file.getContentType();
        validateDocContentType(contentType, file.getOriginalFilename());

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new StorageException("Failed to read document file data: " + e.getMessage(), e);
        }

        return uploadDoc(workerId, docType, bytes, file.getOriginalFilename(), contentType);
    }

    @Override
    public String uploadWorkerDocument(String workerId, String docType, byte[] fileBytes, String originalFilename, String contentType) {
        if (workerId == null || workerId.isBlank()) {
            throw new InvalidFileException("Worker ID cannot be null or empty.");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            throw new InvalidFileException("Document file cannot be null or empty.");
        }
        if (fileBytes.length > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the maximum allowed limit of 5 MB.");
        }

        validateDocContentType(contentType, originalFilename);

        return uploadDoc(workerId, docType, fileBytes, originalFilename, contentType);
    }

    private void validateDocContentType(String contentType, String originalFilename) {
        String cleanType = (contentType != null) ? contentType.toLowerCase().trim() : "";
        if (ALLOWED_DOC_CONTENT_TYPES.contains(cleanType)) {
            return;
        }
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".webp")
                    || ext.equals(".pdf") || ext.equals(".json") || ext.equals(".xlsx") || ext.equals(".xls")
                    || ext.equals(".doc") || ext.equals(".docx") || ext.equals(".csv") || ext.equals(".txt")) {
                return;
            }
        }
        if (cleanType.contains("image/") || cleanType.contains("pdf") || cleanType.contains("spreadsheet")
                || cleanType.contains("excel") || cleanType.contains("officedocument") || cleanType.equals("application/octet-stream")) {
            return;
        }
        throw new InvalidFileException("Invalid file type. Only PNG, JPEG, WebP images and PDF documents are allowed.");
    }

    private String uploadDoc(String workerId, String docType, byte[] fileBytes, String originalFilename, String contentType) {
        String ext = determineDocExtension(contentType, originalFilename);
        String cleanDocType = (docType != null && !docType.isBlank()) ? docType.toLowerCase().trim() : "document";
        String generatedFileName = cleanDocType + "_" + UUID.randomUUID() + ext;
        String storagePath = "workers/" + workerId + "/" + generatedFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storagePath)
                    .contentType(contentType != null ? contentType.toLowerCase().trim() : "application/octet-stream")
                    .contentLength((long) fileBytes.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
            log.info("Successfully uploaded worker document to bucket '{}' at path '{}'", bucket, storagePath);

            if (endpoint != null && endpoint.contains("/storage/v1/s3")) {
                return endpoint.replace("/storage/v1/s3", "/storage/v1/object/public/" + bucket + "/" + storagePath);
            }
            return storagePath;
        } catch (Exception e) {
            log.warn("Warning uploading worker document to Supabase S3: {}", e.getMessage());
            return "https://storage.salaryneeds.app/" + storagePath;
        }
    }

    private String determineDocExtension(String contentType, String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".webp") || ext.equals(".pdf")) {
                return ext;
            }
        }
        if (contentType == null) return ".jpg";
        return switch (contentType.toLowerCase().trim()) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            case "application/json" -> ".json";
            default -> ".jpg";
        };
    }

    private String resolvePublicUrl(String storagePath) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + storagePath;
        }
        if (endpoint != null && endpoint.contains("/storage/v1/s3")) {
            return endpoint.replace("/storage/v1/s3", "/storage/v1/object/public/" + bucket + "/" + storagePath);
        }
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint.replaceAll("/+$", "") + "/" + bucket + "/" + storagePath;
        }
        return "https://api.anjibabujob.com/uploads/" + storagePath;
    }

    @Override
    public String uploadCatalogAsset(String folder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Asset file cannot be null or empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the maximum allowed limit of 5 MB.");
        }

        String contentType = file.getContentType();
        validateContentType(contentType);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new StorageException("Failed to read asset file data: " + e.getMessage(), e);
        }

        return uploadCatalogAsset(folder, bytes, file.getOriginalFilename(), contentType);
    }

    @Override
    public String uploadCatalogAsset(String folder, byte[] fileBytes, String originalFilename, String contentType) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new InvalidFileException("Asset file content cannot be null or empty.");
        }
        if (fileBytes.length > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the maximum allowed limit of 5 MB.");
        }

        validateContentType(contentType);

        String cleanFolder = (folder != null && !folder.isBlank()) ? folder.toLowerCase().trim() : "catalog";
        String extension = determineExtension(contentType, originalFilename);
        String generatedFileName = UUID.randomUUID() + extension;
        String storagePath = cleanFolder + "/" + generatedFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storagePath)
                    .contentType(contentType != null ? contentType.toLowerCase().trim() : "image/jpeg")
                    .contentLength((long) fileBytes.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
            log.info("Successfully uploaded catalog asset to bucket '{}' at path '{}'", bucket, storagePath);
            return resolvePublicUrl(storagePath);
        } catch (Exception e) {
            log.warn("S3 upload failed for catalog asset key '{}': {}. Returning fallback URL.", storagePath, e.getMessage());
            return resolvePublicUrl(storagePath);
        }
    }

    @Override
    public String uploadFromUrl(String folder, String remoteUrl) {
        if (remoteUrl == null || remoteUrl.isBlank()) {
            return null;
        }
        if (!remoteUrl.startsWith("http://") && !remoteUrl.startsWith("https://")) {
            return remoteUrl;
        }

        try {
            java.net.URI uri = java.net.URI.create(remoteUrl);
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .header("User-Agent", "SalaryNeeds-Storage/1.0")
                    .timeout(java.time.Duration.ofSeconds(15))
                    .GET()
                    .build();

            java.net.http.HttpResponse<byte[]> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200 && response.body() != null && response.body().length > 0) {
                String contentType = response.headers().firstValue("Content-Type").orElse("image/jpeg");
                if (contentType.contains(";")) {
                    contentType = contentType.split(";")[0].trim();
                }
                String path = uri.getPath();
                String filename = (path != null && path.contains("/")) ? path.substring(path.lastIndexOf("/") + 1) : "image.jpg";
                return uploadCatalogAsset(folder, response.body(), filename, contentType);
            }
        } catch (Exception e) {
            log.warn("Could not fetch remote image from '{}': {}", remoteUrl, e.getMessage());
        }
        return remoteUrl;
    }
}
