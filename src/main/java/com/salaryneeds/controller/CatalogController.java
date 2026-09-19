package com.salaryneeds.controller;

import com.salaryneeds.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/catalog", "/v1/catalog", "/api/catalog", "/api"})
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping({"/categories", "/catalog/categories"})
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> categories = catalogService.getCategories();
        return ResponseEntity.ok(categories);
    }
}
