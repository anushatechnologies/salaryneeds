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

    /**
     * Uploads a worker document (Aadhaar, PAN) to Supabase Storage under:
     * workers/{workerId}/{docType}_{generatedFileName}
     *
     * @param workerId ID of the worker
     * @param docType  Type of document (e.g. AADHAAR_CARD, PAN_CARD)
     * @param file     Uploaded document file (PNG, JPEG, WebP, PDF; max 5MB)
     * @return The public URL or storage path of the uploaded document
     */
    String uploadWorkerDocument(String workerId, String docType, MultipartFile file);

    /**
     * Uploads a worker document to Supabase Storage directly from raw bytes.
     *
     * @param workerId         ID of the worker
     * @param docType          Type of document (e.g. AADHAAR_CARD, PAN_CARD)
     * @param fileBytes        Document content bytes
     * @param originalFilename Original file name
     * @param contentType      MIME type
     * @return The public URL or storage path of the uploaded document
     */
    String uploadWorkerDocument(String workerId, String docType, byte[] fileBytes, String originalFilename, String contentType);

    /**
     * Uploads a catalog asset (category, subcategory, variant) to S3 Storage under:
     * {folder}/{generatedFileName}
     *
     * @param folder Directory/folder name (e.g. "categories", "subcategories", "variants")
     * @param file   Uploaded image/asset file
     * @return The public URL of the uploaded catalog asset
     */
    String uploadCatalogAsset(String folder, MultipartFile file);

    /**
     * Uploads a catalog asset directly from raw bytes.
     *
     * @param folder           Directory/folder name
     * @param fileBytes        Asset content bytes
     * @param originalFilename Original file name
     * @param contentType      MIME type
     * @return The public URL of the uploaded catalog asset
     */
    String uploadCatalogAsset(String folder, byte[] fileBytes, String originalFilename, String contentType);

    /**
     * Fetches a remote asset URL and uploads it to S3 Storage under:
     * {folder}/{generatedFileName}
     *
     * @param folder    Directory/folder name
     * @param remoteUrl External HTTP/HTTPS image URL to fetch and store
     * @return The public URL in the S3 bucket
     */
    String uploadFromUrl(String folder, String remoteUrl);

    /**
     * Uploads structured JSON metadata for catalog entities to S3 Storage.
     *
     * @param storagePath Explicit key/path in S3 (e.g. "catalog/catalog-tree.json", "categories/{id}/category.json")
     * @param jsonBytes   UTF-8 encoded JSON bytes
     * @return The public URL of the uploaded JSON object in S3
     */
    String uploadCatalogJson(String storagePath, byte[] jsonBytes);
}
