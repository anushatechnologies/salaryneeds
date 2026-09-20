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

    private final S3Client s3Client;

    @Value("${SUPABASE_CATALOG_BUCKET:${supabase.catalog.bucket:salaryneeds}}")
    private String bucket;

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
}
