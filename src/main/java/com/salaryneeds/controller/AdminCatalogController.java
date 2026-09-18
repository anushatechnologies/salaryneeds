package com.salaryneeds.controller;

import com.salaryneeds.dto.catalog.*;
import com.salaryneeds.service.CatalogManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/admin", "/api/v1/admin"})
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogManagementService catalogManagementService;

    // ==========================================
    // 1. SERVICE (TOP LEVEL)
    // ==========================================

    @PostMapping("/services")
    public ResponseEntity<ServiceResponseDTO> createService(@Valid @RequestBody ServiceRequestDTO request) {
        ServiceResponseDTO response = catalogManagementService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponseDTO>> getAllServices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        List<ServiceResponseDTO> services = catalogManagementService.getAllServices(search, status);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> getServiceById(
            @PathVariable UUID serviceId,
            @RequestParam(defaultValue = "true") boolean includeCategories
    ) {
        ServiceResponseDTO response = catalogManagementService.getServiceById(serviceId, includeCategories);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/services/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> updateService(
            @PathVariable UUID serviceId,
            @RequestBody ServiceRequestDTO request
    ) {
        ServiceResponseDTO response = catalogManagementService.updateService(serviceId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/services/{serviceId}/status")
    public ResponseEntity<ServiceResponseDTO> updateServiceStatus(
            @PathVariable UUID serviceId,
            @Valid @RequestBody StatusUpdateRequestDTO request
    ) {
        ServiceResponseDTO response = catalogManagementService.updateServiceStatus(serviceId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/services/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID serviceId) {
        catalogManagementService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 2. CATEGORY (MIDDLE LEVEL UNDER SERVICE)
    // ==========================================

    @PostMapping("/services/{serviceId}/categories")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @PathVariable UUID serviceId,
            @Valid @RequestBody CategoryRequestDTO request
    ) {
        CategoryResponseDTO response = catalogManagementService.createCategory(serviceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/services/{serviceId}/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getCategoriesByService(
            @PathVariable UUID serviceId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        List<CategoryResponseDTO> response = catalogManagementService.getCategoriesByService(serviceId, search, status);
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
    // 3. SUB-CATEGORY (LEAF LEVEL UNDER CATEGORY)
    // ==========================================

    @PostMapping("/categories/{categoryId}/sub-categories")
    public ResponseEntity<SubCategoryResponseDTO> createSubCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody SubCategoryRequestDTO request
    ) {
        SubCategoryResponseDTO response = catalogManagementService.createSubCategory(categoryId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/categories/{categoryId}/sub-categories")
    public ResponseEntity<List<SubCategoryResponseDTO>> getSubCategoriesByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) String status
    ) {
        List<SubCategoryResponseDTO> response = catalogManagementService.getSubCategoriesByCategory(categoryId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sub-categories/{subCategoryId}")
    public ResponseEntity<SubCategoryResponseDTO> getSubCategoryById(@PathVariable Long subCategoryId) {
        SubCategoryResponseDTO response = catalogManagementService.getSubCategoryById(subCategoryId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sub-categories/{subCategoryId}")
    public ResponseEntity<SubCategoryResponseDTO> updateSubCategory(
            @PathVariable Long subCategoryId,
            @RequestBody SubCategoryRequestDTO request
    ) {
        SubCategoryResponseDTO response = catalogManagementService.updateSubCategory(subCategoryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/sub-categories/{subCategoryId}/status")
    public ResponseEntity<SubCategoryResponseDTO> updateSubCategoryStatus(
            @PathVariable Long subCategoryId,
            @Valid @RequestBody StatusUpdateRequestDTO request
    ) {
        SubCategoryResponseDTO response = catalogManagementService.updateSubCategoryStatus(subCategoryId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sub-categories/{subCategoryId}")
    public ResponseEntity<Void> deleteSubCategory(@PathVariable Long subCategoryId) {
        catalogManagementService.deleteSubCategory(subCategoryId);
        return ResponseEntity.noContent().build();
    }
}
