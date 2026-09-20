package com.salaryneeds.controller;

import com.salaryneeds.dto.ApiResponse;
import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;
import com.salaryneeds.service.CatalogManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserCatalogController {

    private final CatalogManagementService catalogManagementService;

    @GetMapping({"/api/user/categories", "/user/categories", "/api/categories/active"})
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategories() {
        List<CategoryResponseDTO> categories = catalogManagementService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", categories));
    }

    @GetMapping({
            "/api/categories/{categoryId}/subcategories",
            "/categories/{categoryId}/subcategories",
            "/api/user/categories/{categoryId}/subcategories",
            "/user/categories/{categoryId}/subcategories"
    })
    public ResponseEntity<ApiResponse<List<SubCategoryResponseDTO>>> getSubcategoriesByCategory(
            @PathVariable UUID categoryId
    ) {
        List<SubCategoryResponseDTO> subcategories = catalogManagementService.getActiveSubCategoriesByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Active subcategories retrieved successfully", subcategories));
    }

    @GetMapping({
            "/api/subcategories/{subcategoryId}",
            "/subcategories/{subcategoryId}",
            "/api/user/subcategories/{subcategoryId}",
            "/user/subcategories/{subcategoryId}"
    })
    public ResponseEntity<ApiResponse<SubCategoryResponseDTO>> getSubcategoryById(
            @PathVariable Long subcategoryId
    ) {
        SubCategoryResponseDTO subcategory = catalogManagementService.getActiveSubCategoryById(subcategoryId);
        return ResponseEntity.ok(ApiResponse.success("Active subcategory retrieved successfully", subcategory));
    }

    @GetMapping({
            "/api/subcategories/{subcategoryId}/variants",
            "/subcategories/{subcategoryId}/variants",
            "/api/user/subcategories/{subcategoryId}/variants",
            "/user/subcategories/{subcategoryId}/variants"
    })
    public ResponseEntity<ApiResponse<List<VariantResponseDTO>>> getVariantsBySubcategory(
            @PathVariable Long subcategoryId
    ) {
        List<VariantResponseDTO> variants = catalogManagementService.getActiveVariantsBySubCategory(subcategoryId);
        return ResponseEntity.ok(ApiResponse.success("Active variants retrieved successfully", variants));
    }
}
