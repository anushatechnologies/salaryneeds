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
@RequestMapping({"/api/user", "/user", "/api", ""})
@RequiredArgsConstructor
public class UserCatalogController {

    private final CatalogManagementService catalogManagementService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategories() {
        List<CategoryResponseDTO> categories = catalogManagementService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", categories));
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<ApiResponse<List<SubCategoryResponseDTO>>> getSubcategoriesByCategory(
            @PathVariable UUID categoryId
    ) {
        List<SubCategoryResponseDTO> subcategories = catalogManagementService.getActiveSubCategoriesByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Active subcategories retrieved successfully", subcategories));
    }

    @GetMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<ApiResponse<SubCategoryResponseDTO>> getSubcategoryById(
            @PathVariable Long subcategoryId
    ) {
        SubCategoryResponseDTO subcategory = catalogManagementService.getActiveSubCategoryById(subcategoryId);
        return ResponseEntity.ok(ApiResponse.success("Active subcategory retrieved successfully", subcategory));
    }

    @GetMapping("/subcategories/{subcategoryId}/variants")
    public ResponseEntity<ApiResponse<List<VariantResponseDTO>>> getVariantsBySubcategory(
            @PathVariable Long subcategoryId
    ) {
        List<VariantResponseDTO> variants = catalogManagementService.getActiveVariantsBySubCategory(subcategoryId);
        return ResponseEntity.ok(ApiResponse.success("Active variants retrieved successfully", variants));
    }
}
