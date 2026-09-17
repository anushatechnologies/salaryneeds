package com.salaryneeds.controller;

import com.salaryneeds.dto.CategoryDTO;
import com.salaryneeds.dto.ServiceItemDTO;
import com.salaryneeds.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        return ResponseEntity.ok(catalogService.getAllCategories());
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceItemDTO>> getServices(
            @RequestParam(value = "category_id", required = false) UUID categoryId
    ) {
        if (categoryId != null) {
            return ResponseEntity.ok(catalogService.getServicesByCategory(categoryId));
        }
        return ResponseEntity.ok(catalogService.getAllServices());
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<ServiceItemDTO> getServiceById(@PathVariable Long serviceId) {
        return ResponseEntity.ok(catalogService.getServiceById(serviceId));
    }
}
