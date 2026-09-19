package com.salaryneeds.controller;

import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;
import com.salaryneeds.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getActiveCategories() {
        List<CategoryResponseDTO> categories = catalogService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<List<SubCategoryResponseDTO>> getActiveSubcategories(@PathVariable UUID categoryId) {
        List<SubCategoryResponseDTO> subcategories = catalogService.getActiveSubcategoriesByCategory(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<SubCategoryResponseDTO> getActiveSubcategoryById(@PathVariable Long subcategoryId) {
        SubCategoryResponseDTO subcategory = catalogService.getActiveSubcategoryById(subcategoryId);
        return ResponseEntity.ok(subcategory);
    }

    @GetMapping("/subcategories/{subcategoryId}/variants")
    public ResponseEntity<List<VariantResponseDTO>> getActiveVariantsBySubcategory(@PathVariable Long subcategoryId) {
        List<VariantResponseDTO> variants = catalogService.getActiveVariantsBySubcategory(subcategoryId);
        return ResponseEntity.ok(variants);
    }
}
