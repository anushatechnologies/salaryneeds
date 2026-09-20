package com.salaryneeds.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Reusable storage service for S3-compatible Supabase Storage.
 */
public interface SupabaseStorageService {

    /**
     * Uploads a category image to Supabase Storage under:
     * categories/{categoryId}/{generatedFileName}
     *
     * @param categoryId ID of the category
     * @param file       Uploaded image file (PNG, JPEG, WebP; max 5MB)
     * @return The storage path formatted as categories/{categoryId}/{generatedFileName}
     */
    String uploadCategoryImage(UUID categoryId, MultipartFile file);

    /**
     * Uploads a category image to Supabase Storage directly from raw bytes.
     *
     * @param categoryId       ID of the category
     * @param fileBytes        Image content bytes
     * @param originalFilename Original file name (used for extension detection)
     * @param contentType      MIME type (image/png, image/jpeg, image/webp)
     * @return The storage path formatted as categories/{categoryId}/{generatedFileName}
     */
    String uploadCategoryImage(UUID categoryId, byte[] fileBytes, String originalFilename, String contentType);
}
