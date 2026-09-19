package com.salaryneeds.service.storage;

import com.salaryneeds.exception.InvalidFileException;
import com.salaryneeds.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private SupabaseStorageServiceImpl storageService;

    private final UUID categoryId = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private final String bucket = "salaryneeds";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "bucket", bucket);
    }

    @Test
    @DisplayName("1. Valid PNG image upload - Successfully uploads and returns storage path")
    void testUploadCategoryImage_ValidPng_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "cleaning-icon.png",
                "image/png",
                "dummy-png-bytes".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String path = storageService.uploadCategoryImage(categoryId, file);

        assertNotNull(path);
        assertTrue(path.startsWith("categories/" + categoryId + "/"));
        assertTrue(path.endsWith(".png"));

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(bucket, capturedRequest.bucket());
        assertEquals(path, capturedRequest.key());
        assertEquals("image/png", capturedRequest.contentType());
    }

    @Test
    @DisplayName("2. Valid JPEG image upload - Successfully uploads and returns storage path")
    void testUploadCategoryImage_ValidJpeg_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "service.jpeg",
                "image/jpeg",
                "dummy-jpeg-bytes".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String path = storageService.uploadCategoryImage(categoryId, file);

        assertNotNull(path);
        assertTrue(path.startsWith("categories/" + categoryId + "/"));
        assertTrue(path.endsWith(".jpeg"));
    }

    @Test
    @DisplayName("3. Valid WebP image upload - Successfully uploads and returns storage path")
    void testUploadCategoryImage_ValidWebp_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "category.webp",
                "image/webp",
                "dummy-webp-bytes".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String path = storageService.uploadCategoryImage(categoryId, file);

        assertNotNull(path);
        assertTrue(path.startsWith("categories/" + categoryId + "/"));
        assertTrue(path.endsWith(".webp"));
    }

    @Test
    @DisplayName("4. Valid raw bytes upload - Successfully uploads and returns storage path")
    void testUploadCategoryImage_RawBytes_Success() {
        byte[] bytes = "raw-image-bytes".getBytes();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String path = storageService.uploadCategoryImage(categoryId, bytes, "banner.png", "image/png");

        assertNotNull(path);
        assertTrue(path.startsWith("categories/" + categoryId + "/"));
        assertTrue(path.endsWith(".png"));
    }

    @Test
    @DisplayName("5. Invalid file type (PDF/text) - Rejects with InvalidFileException")
    void testUploadCategoryImage_InvalidFileType_ThrowsException() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "image",
                "document.pdf",
                "application/pdf",
                "dummy-pdf-content".getBytes()
        );

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> storageService.uploadCategoryImage(categoryId, pdfFile)
        );

        assertTrue(ex.getMessage().contains("Invalid file type"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("6. File exceeds 5 MB - Rejects with InvalidFileException")
    void testUploadCategoryImage_FileTooLarge_ThrowsException() {
        // Create file larger than 5 MB
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile largeFile = new MockMultipartFile(
                "image",
                "huge-image.png",
                "image/png",
                largeBytes
        );

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> storageService.uploadCategoryImage(categoryId, largeFile)
        );

        assertTrue(ex.getMessage().contains("5 MB"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("7. Empty file - Rejects with InvalidFileException")
    void testUploadCategoryImage_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.png",
                "image/png",
                new byte[0]
        );

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> storageService.uploadCategoryImage(categoryId, emptyFile)
        );

        assertTrue(ex.getMessage().contains("empty"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("8. Null Category ID - Rejects with InvalidFileException")
    void testUploadCategoryImage_NullCategoryId_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "icon.png",
                "image/png",
                "bytes".getBytes()
        );

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> storageService.uploadCategoryImage(null, file)
        );

        assertTrue(ex.getMessage().contains("Category ID"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("9. Storage upload failure (S3Exception) - Throws StorageException")
    void testUploadCategoryImage_StorageUploadFailure_ThrowsStorageException() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "icon.png",
                "image/png",
                "dummy-png-bytes".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("Access Denied (403)").statusCode(403).build());

        StorageException ex = assertThrows(
                StorageException.class,
                () -> storageService.uploadCategoryImage(categoryId, file)
        );

        assertTrue(ex.getMessage().contains("Failed to upload"));
        assertNotNull(ex.getCause());
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
