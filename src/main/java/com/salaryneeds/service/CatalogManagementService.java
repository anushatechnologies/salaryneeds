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

    // --- 2. Category (Middle Level) Operations ---
    CategoryResponseDTO createCategory(UUID serviceId, CategoryRequestDTO request);

    List<CategoryResponseDTO> getCategoriesByService(UUID serviceId, String search, String status);

    CategoryResponseDTO getCategoryById(UUID categoryId, boolean includeSubCategories);

    CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO request);

    CategoryResponseDTO updateCategoryStatus(UUID categoryId, String status);

    void deleteCategory(UUID categoryId);

    // --- 3. Sub-Category (Leaf Level) Operations ---
    SubCategoryResponseDTO createSubCategory(UUID categoryId, SubCategoryRequestDTO request);

    List<SubCategoryResponseDTO> getSubCategoriesByCategory(UUID categoryId, String status);

    SubCategoryResponseDTO getSubCategoryById(Long subCategoryId);

    SubCategoryResponseDTO updateSubCategory(Long subCategoryId, SubCategoryRequestDTO request);

    SubCategoryResponseDTO updateSubCategoryStatus(Long subCategoryId, String status);

    void deleteSubCategory(Long subCategoryId);
}
