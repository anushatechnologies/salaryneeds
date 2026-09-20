package com.salaryneeds.controller;

import com.salaryneeds.dto.catalog.*;
import com.salaryneeds.service.CatalogManagementService;
import com.salaryneeds.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/admin", "/admin", "/api/v1/admin"})
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogManagementService catalogManagementService;
    private final FileStorageService fileStorageService;

    // ==========================================
    // 0. IMAGE UPLOAD
    // ==========================================

    @PostMapping(value = "/uploads/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> uploadImage(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        MultipartFile upload = (file != null && !file.isEmpty()) ? file : image;
        if (upload == null || upload.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        String imageUrl = fileStorageService.store(upload, "catalog");
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("imageUrl", imageUrl);
        response.put("image", imageUrl);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 1. CATEGORY MODULE (TIER 1)
    // ==========================================

    @PostMapping(value = "/categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponseDTO> createCategoryMultipart(
            @ModelAttribute CategoryRequestDTO request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        MultipartFile upload = (file != null && !file.isEmpty()) ? file : image;
        if (upload != null && !upload.isEmpty()) {
            String imageUrl = fileStorageService.store(upload, "categories");
            request.setImageUrl(imageUrl);
        }
        CategoryResponseDTO response = catalogManagementService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO request
    ) {
        CategoryResponseDTO response = catalogManagementService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/categories/{categoryId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponseDTO> uploadCategoryImage(
            @PathVariable UUID categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        MultipartFile upload = (file != null && !file.isEmpty()) ? file : image;
        if (upload == null || upload.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        String imageUrl = fileStorageService.store(upload, "categories");
        CategoryRequestDTO updateRequest = CategoryRequestDTO.builder()
                .imageUrl(imageUrl)
                .build();
        CategoryResponseDTO response = catalogManagementService.updateCategory(categoryId, updateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        List<CategoryResponseDTO> response = catalogManagementService.getAllCategories(search, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "true") boolean includeSubCategories
    ) {
        CategoryResponseDTO response = catalogManagementService.getCategoryById(categoryId, includeSubCategories);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable UUID categoryId,
            @RequestBody CategoryRequestDTO request
    ) {
        CategoryResponseDTO response = catalogManagementService.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/categories/{categoryId}/status")
    public ResponseEntity<CategoryResponseDTO> updateCategoryStatus(
            @PathVariable UUID categoryId,
            @Valid @RequestBody StatusUpdateRequestDTO request
    ) {
        CategoryResponseDTO response = catalogManagementService.updateCategoryStatus(categoryId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        catalogManagementService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 2. SUBCATEGORY / SERVICE MODULE
    // ==========================================

    @PostMapping(value = {"/subcategories", "/categories/{categoryId}/sub-categories", "/categories/{categoryId}/subcategories"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubCategoryResponseDTO> createSubCategoryMultipart(
            @PathVariable(required = false) UUID categoryId,
            @ModelAttribute SubCategoryRequestDTO request,
            @RequestParam(value = "uploadImage", required = false) MultipartFile uploadImageParam,
            @RequestParam(value = "upload_image", required = false) MultipartFile uploadImageSnake,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        MultipartFile upload = uploadImageParam != null && !uploadImageParam.isEmpty() ? uploadImageParam :
                (uploadImageSnake != null && !uploadImageSnake.isEmpty() ? uploadImageSnake :
                (file != null && !file.isEmpty() ? file : image));

        if (upload != null && !upload.isEmpty()) {
            String imageUrl = fileStorageService.store(upload, "subcategories");
            request.setImageUrl(imageUrl);
        }
        SubCategoryResponseDTO response;
        if (categoryId != null) {
            request.setCategoryId(categoryId);
            response = catalogManagementService.createSubCategory(categoryId, request);
        } else {
            response = catalogManagementService.createSubCategory(request);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping({"/subcategories", "/categories/{categoryId}/sub-categories", "/categories/{categoryId}/subcategories"})
    public ResponseEntity<SubCategoryResponseDTO> createSubCategory(
            @PathVariable(required = false) UUID categoryId,
            @Valid @RequestBody SubCategoryRequestDTO request
    ) {
        SubCategoryResponseDTO response;
        if (categoryId != null) {
            request.setCategoryId(categoryId);
            response = catalogManagementService.createSubCategory(categoryId, request);
        } else {
            response = catalogManagementService.createSubCategory(request);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"/subcategories", "/categories/{categoryId}/sub-categories", "/categories/{categoryId}/subcategories"})
    public ResponseEntity<List<SubCategoryResponseDTO>> getSubCategories(
            @PathVariable(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        List<SubCategoryResponseDTO> response;
        if (categoryId != null) {
            response = catalogManagementService.getSubCategoriesByCategory(categoryId, status);
        } else {
            response = catalogManagementService.getAllSubCategories(null, search, status);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/subcategories/{subCategoryId}", "/sub-categories/{subCategoryId}"})
    public ResponseEntity<SubCategoryResponseDTO> getSubCategoryById(
            @PathVariable Long subCategoryId,
            @RequestParam(defaultValue = "true") boolean includeVariants
    ) {
        SubCategoryResponseDTO response = catalogManagementService.getSubCategoryById(subCategoryId, includeVariants);
        return ResponseEntity.ok(response);
    }

    @PutMapping({"/subcategories/{subCategoryId}", "/sub-categories/{subCategoryId}"})
    public ResponseEntity<SubCategoryResponseDTO> updateSubCategory(
            @PathVariable Long subCategoryId,
            @RequestBody SubCategoryRequestDTO request
    ) {
        SubCategoryResponseDTO response = catalogManagementService.updateSubCategory(subCategoryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping({"/subcategories/{subCategoryId}/status", "/sub-categories/{subCategoryId}/status"})
    public ResponseEntity<SubCategoryResponseDTO> updateSubCategoryStatus(
            @PathVariable Long subCategoryId,
            @Valid @RequestBody StatusUpdateRequestDTO request
    ) {
        SubCategoryResponseDTO response = catalogManagementService.updateSubCategoryStatus(subCategoryId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping({"/subcategories/{subCategoryId}", "/sub-categories/{subCategoryId}"})
    public ResponseEntity<Void> deleteSubCategory(@PathVariable Long subCategoryId) {
        catalogManagementService.deleteSubCategory(subCategoryId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 3. OPTIONAL VARIANT MODULE
    // ==========================================

    @PostMapping(value = {"/subcategories/{subCategoryId}/variants", "/sub-categories/{subCategoryId}/variants"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VariantResponseDTO> createVariantMultipart(
            @PathVariable Long subCategoryId,
            @ModelAttribute VariantRequestDTO request,
            @RequestParam(value = "variantImage", required = false) MultipartFile variantImage,
            @RequestParam(value = "variant_image", required = false) MultipartFile variantImageSnake,
            @RequestParam(value = "uploadImage", required = false) MultipartFile uploadImage,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        MultipartFile upload = variantImage != null && !variantImage.isEmpty() ? variantImage :
                (variantImageSnake != null && !variantImageSnake.isEmpty() ? variantImageSnake :
                (uploadImage != null && !uploadImage.isEmpty() ? uploadImage :
                (file != null && !file.isEmpty() ? file : image)));

        if (upload != null && !upload.isEmpty()) {
            String imageUrl = fileStorageService.store(upload, "variants");
            request.setImageUrl(imageUrl);
        }
        VariantResponseDTO response = catalogManagementService.createVariant(subCategoryId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping({"/subcategories/{subCategoryId}/variants", "/sub-categories/{subCategoryId}/variants"})
    public ResponseEntity<VariantResponseDTO> createVariant(
            @PathVariable Long subCategoryId,
            @Valid @RequestBody VariantRequestDTO request
    ) {
        VariantResponseDTO response = catalogManagementService.createVariant(subCategoryId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"/subcategories/{subCategoryId}/variants", "/sub-categories/{subCategoryId}/variants"})
    public ResponseEntity<List<VariantResponseDTO>> getVariantsBySubCategory(
            @PathVariable Long subCategoryId,
            @RequestParam(required = false) String status
    ) {
        List<VariantResponseDTO> response = catalogManagementService.getVariantsBySubCategory(subCategoryId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<VariantResponseDTO> getVariantById(@PathVariable Long variantId) {
        VariantResponseDTO response = catalogManagementService.getVariantById(variantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<VariantResponseDTO> updateVariant(
            @PathVariable Long variantId,
            @RequestBody VariantRequestDTO request
    ) {
        VariantResponseDTO response = catalogManagementService.updateVariant(variantId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/variants/{variantId}/status")
    public ResponseEntity<VariantResponseDTO> updateVariantStatus(
            @PathVariable Long variantId,
            @Valid @RequestBody StatusUpdateRequestDTO request
    ) {
        VariantResponseDTO response = catalogManagementService.updateVariantStatus(variantId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long variantId) {
        catalogManagementService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. PURGE / CLEAR ALL DATABASE DATA
    // ==========================================

    @DeleteMapping({"/clear-all-data", "/catalog/clear-all", "/categories/clear-all"})
    public ResponseEntity<java.util.Map<String, Object>> clearAllCatalogData() {
        catalogManagementService.clearAllCatalogData();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("message", "All catalog data (categories, subcategories, variants) removed successfully from database");
        return ResponseEntity.ok(response);
    }
}
