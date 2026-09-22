package com.salaryneeds;

import com.salaryneeds.service.FileStorageService;
import com.salaryneeds.service.storage.SupabaseStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CatalogS3StorageTest {

    private S3Client s3Client;
    private SupabaseStorageServiceImpl supabaseStorageService;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        s3Client = Mockito.mock(S3Client.class);
        supabaseStorageService = new SupabaseStorageServiceImpl(s3Client);
        ReflectionTestUtils.setField(supabaseStorageService, "bucket", "salaryneeds");
        ReflectionTestUtils.setField(supabaseStorageService, "endpoint", "https://jbgctbgqymrdurkumwes.storage.supabase.co/storage/v1/s3");

        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "supabaseStorageService", supabaseStorageService);
    }

    @Test
    @DisplayName("Upload Category asset sends PutObjectRequest with categories/ key prefix to S3")
    void testUploadCategoryAsset() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "cleaning.jpg", "image/jpeg", "dummy-image-content".getBytes()
        );

        String resultUrl = supabaseStorageService.uploadCatalogAsset("categories", file);

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("/storage/v1/object/public/salaryneeds/categories/"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest captured = captor.getValue();
        assertEquals("salaryneeds", captured.bucket());
        assertTrue(captured.key().startsWith("categories/"));
        assertTrue(captured.key().endsWith(".jpg"));
        assertEquals("image/jpeg", captured.contentType());
    }

    @Test
    @DisplayName("Upload SubCategory asset sends PutObjectRequest with subcategories/ key prefix to S3")
    void testUploadSubCategoryAsset() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "sofa.png", "image/png", "dummy-image-content".getBytes()
        );

        String resultUrl = supabaseStorageService.uploadCatalogAsset("subcategories", file);

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("/storage/v1/object/public/salaryneeds/subcategories/"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest captured = captor.getValue();
        assertEquals("salaryneeds", captured.bucket());
        assertTrue(captured.key().startsWith("subcategories/"));
        assertTrue(captured.key().endsWith(".png"));
    }

    @Test
    @DisplayName("Upload Variant asset sends PutObjectRequest with variants/ key prefix to S3")
    void testUploadVariantAsset() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "sofa-3-seater.webp", "image/webp", "dummy-image-content".getBytes()
        );

        String resultUrl = supabaseStorageService.uploadCatalogAsset("variants", file);

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("/storage/v1/object/public/salaryneeds/variants/"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest captured = captor.getValue();
        assertEquals("salaryneeds", captured.bucket());
        assertTrue(captured.key().startsWith("variants/"));
        assertTrue(captured.key().endsWith(".webp"));
    }

    @Test
    @DisplayName("FileStorageService routes category uploads to SupabaseStorageService uploadCatalogAsset")
    void testFileStorageServiceRoutesToCatalog() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "ac.jpg", "image/jpeg", "image-content".getBytes()
        );

        String resultUrl = fileStorageService.store(file, "categories");

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("categories/"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Upload Catalog JSON sends PutObjectRequest with application/json contentType")
    void testUploadCatalogJson() {
        byte[] jsonBytes = "{\"test\": \"data\"}".getBytes();
        String resultUrl = supabaseStorageService.uploadCatalogJson("catalog/catalog-tree.json", jsonBytes);

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("/storage/v1/object/public/salaryneeds/catalog/catalog-tree.json"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest captured = captor.getValue();
        assertEquals("salaryneeds", captured.bucket());
        assertEquals("catalog/catalog-tree.json", captured.key());
        assertEquals("application/json", captured.contentType());
    }
}
