package com.salaryneeds.service;

import com.salaryneeds.dto.catalog.*;

import java.util.List;
import java.util.UUID;

public interface CatalogManagementService {

    // --- 1. Service (Top Level) Operations ---
    ServiceResponseDTO createService(ServiceRequestDTO request);

    List<ServiceResponseDTO> getAllServices(String search, String status);

    ServiceResponseDTO getServiceById(UUID serviceId, boolean includeCategories);

    ServiceResponseDTO updateService(UUID serviceId, ServiceRequestDTO request);

    ServiceResponseDTO updateServiceStatus(UUID serviceId, String status);

    void deleteService(UUID serviceId);

    // --- 2. Category Operations ---
    CategoryResponseDTO createCategory(CategoryRequestDTO request);

    CategoryResponseDTO createCategory(UUID serviceId, CategoryRequestDTO request);

    List<CategoryResponseDTO> getAllCategories(String search, String status);

    List<CategoryResponseDTO> getCategoriesByService(UUID serviceId, String search, String status);

    CategoryResponseDTO getCategoryById(UUID categoryId, boolean includeSubCategories);

    CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO request);

    CategoryResponseDTO updateCategoryStatus(UUID categoryId, String status);

    void deleteCategory(UUID categoryId);

    // --- 3. Sub-Category / Service Operations ---
    SubCategoryResponseDTO createSubCategory(SubCategoryRequestDTO request);

    SubCategoryResponseDTO createSubCategory(UUID categoryId, SubCategoryRequestDTO request);

    List<SubCategoryResponseDTO> getAllSubCategories(UUID categoryId, String search, String status);

    List<SubCategoryResponseDTO> getSubCategoriesByCategory(UUID categoryId, String status);

    SubCategoryResponseDTO getSubCategoryById(Long subCategoryId);

    SubCategoryResponseDTO getSubCategoryById(Long subCategoryId, boolean includeVariants);

    SubCategoryResponseDTO updateSubCategory(Long subCategoryId, SubCategoryRequestDTO request);

    SubCategoryResponseDTO updateSubCategoryStatus(Long subCategoryId, String status);

    void deleteSubCategory(Long subCategoryId);

    // --- 4. Optional Variant Operations ---
    VariantResponseDTO createVariant(Long subCategoryId, VariantRequestDTO request);

    List<VariantResponseDTO> getVariantsBySubCategory(Long subCategoryId, String status);

    VariantResponseDTO getVariantById(Long variantId);

    VariantResponseDTO updateVariant(Long variantId, VariantRequestDTO request);

    VariantResponseDTO updateVariantStatus(Long variantId, String status);

    void deleteVariant(Long variantId);

    // --- 5. User & Worker Active Catalog Operations ---
    List<CategoryResponseDTO> getActiveCategories();

    List<SubCategoryResponseDTO> getActiveSubCategoriesByCategory(UUID categoryId);

    SubCategoryResponseDTO getActiveSubCategoryById(Long subCategoryId);

    List<VariantResponseDTO> getActiveVariantsBySubCategory(Long subCategoryId);
}
