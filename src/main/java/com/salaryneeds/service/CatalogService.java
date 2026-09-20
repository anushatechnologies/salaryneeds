package com.salaryneeds.service;

import com.salaryneeds.dto.CategoryDTO;
import com.salaryneeds.dto.ServiceItemDTO;
import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CatalogService {

    // Existing methods for backward compatibility
    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(UUID categoryId);

    List<ServiceItemDTO> getServicesByCategory(UUID categoryId);

    List<ServiceItemDTO> getAllServices();

    ServiceItemDTO getServiceById(Long serviceId);

    // Public active catalog methods
    List<CategoryResponseDTO> getActiveCategories();

    List<SubCategoryResponseDTO> getActiveSubcategoriesByCategory(UUID categoryId);

    SubCategoryResponseDTO getActiveSubcategoryById(Long subcategoryId);

    List<VariantResponseDTO> getActiveVariantsBySubcategory(Long subcategoryId);
}
