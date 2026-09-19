package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.AdminCatalogController;
import com.salaryneeds.controller.PublicCatalogController;
import com.salaryneeds.dto.catalog.CategoryRequestDTO;
import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryRequestDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantRequestDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;
import com.salaryneeds.exception.CategoryNotFoundException;
import com.salaryneeds.exception.GlobalExceptionHandler;
import com.salaryneeds.exception.SubCategoryNotFoundException;
import com.salaryneeds.security.AdminAuthInterceptor;
import com.salaryneeds.service.CatalogManagementService;
import com.salaryneeds.service.CatalogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AdminCatalogController.class, PublicCatalogController.class})
@Import({GlobalExceptionHandler.class, AdminAuthInterceptor.class})
public class CatalogHierarchyAndVariantTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogManagementService catalogManagementService;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private com.salaryneeds.service.FileStorageService fileStorageService;

    @Test
    @DisplayName("Edge Case 1: Category Name submitted in UPPERCASE is returned normalized in lowercase with amount, discount, and final amount")
    void testCategoryNameSubmittedInUppercaseStoresInLowercase() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("HOME CLEANING")
                .description("Complete residential home cleaning")
                .amount(BigDecimal.valueOf(1000))
                .discount(BigDecimal.valueOf(15))
                .status("ACTIVE")
                .build();

        CategoryResponseDTO response = CategoryResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("home cleaning") // Backend normalized
                .description("Complete residential home cleaning")
                .amount(BigDecimal.valueOf(1000))
                .discount(BigDecimal.valueOf(15))
                .finalAmount(BigDecimal.valueOf(850)) // 1000 - 15% = 850
                .status("ACTIVE")
                .isActive(true)
                .build();

        when(catalogManagementService.createCategory(any(CategoryRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/categories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("home cleaning"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.discount").value(15))
                .andExpect(jsonPath("$.finalAmount").value(850))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Edge Case 2: Subcategory creation calculates final amount on backend")
    void testSubcategoryCreationCalculatesFinalAmount() throws Exception {
        UUID categoryId = UUID.randomUUID();
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .categoryId(categoryId)
                .name("Kitchen Cleaning")
                .description("Complete kitchen deep cleaning")
                .amount(BigDecimal.valueOf(1500))
                .discount(BigDecimal.valueOf(10))
                .status("ACTIVE")
                .build();

        SubCategoryResponseDTO response = SubCategoryResponseDTO.builder()
                .id(1L)
                .categoryId(categoryId)
                .categoryName("home cleaning")
                .name("kitchen cleaning")
                .description("Complete kitchen deep cleaning")
                .amount(BigDecimal.valueOf(1500))
                .discount(BigDecimal.valueOf(10))
                .finalAmount(BigDecimal.valueOf(1350)) // 1500 - (1500 * 10 / 100) = 1350
                .status("ACTIVE")
                .build();

        when(catalogManagementService.createSubCategory(any(SubCategoryRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/subcategories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("kitchen cleaning"))
                .andExpect(jsonPath("$.amount").value(1500))
                .andExpect(jsonPath("$.discount").value(10))
                .andExpect(jsonPath("$.finalAmount").value(1350));
    }

    @Test
    @DisplayName("Edge Case 3: Subcategory without category returns 400 Validation Error")
    void testSubcategoryMissingCategoryReturns400() throws Exception {
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .name("") // Blank name
                .amount(BigDecimal.valueOf(1500))
                .build();

        mockMvc.perform(post("/api/admin/subcategories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Edge Case 4: Variant creation calculates final price: 2000 with 10% discount -> 1800")
    void testVariantCreationWithBackendCalculatedFinalPrice() throws Exception {
        VariantRequestDTO request = VariantRequestDTO.builder()
                .name("1.5 Ton")
                .description("1.5 Ton AC repair service")
                .amount(BigDecimal.valueOf(2000))
                .discount(BigDecimal.valueOf(10))
                .status("ACTIVE")
                .build();

        VariantResponseDTO response = VariantResponseDTO.builder()
                .id(10L)
                .subcategoryId(3L)
                .subcategoryName("ac repair")
                .name("1.5 ton")
                .description("1.5 Ton AC repair service")
                .amount(BigDecimal.valueOf(2000))
                .discount(BigDecimal.valueOf(10))
                .finalAmount(BigDecimal.valueOf(1800)) // 2000 - (2000 * 10 / 100) = 1800
                .status("ACTIVE")
                .build();

        when(catalogManagementService.createVariant(eq(3L), any(VariantRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/subcategories/3/variants")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("1.5 ton"))
                .andExpect(jsonPath("$.amount").value(2000))
                .andExpect(jsonPath("$.finalAmount").value(1800));
    }

    @Test
    @DisplayName("Edge Case 5: Variant creation under non-existing Subcategory returns 404")
    void testVariantUnderNonExistingSubcategoryReturns404() throws Exception {
        VariantRequestDTO request = VariantRequestDTO.builder()
                .name("2 Ton")
                .amount(BigDecimal.valueOf(2500))
                .build();

        when(catalogManagementService.createVariant(eq(999L), any(VariantRequestDTO.class)))
                .thenThrow(new SubCategoryNotFoundException("Sub-Category not found with ID: 999"));

        mockMvc.perform(post("/api/admin/subcategories/999/variants")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Sub-Category Not Found"));
    }

    @Test
    @DisplayName("Edge Case 6: Public APIs return only ACTIVE categories, subcategories, and variants")
    void testPublicApisReturnOnlyActiveItems() throws Exception {
        UUID catId = UUID.randomUUID();
        CategoryResponseDTO activeCat = CategoryResponseDTO.builder()
                .id(catId)
                .name("home cleaning")
                .amount(BigDecimal.valueOf(1000))
                .discount(BigDecimal.valueOf(15))
                .finalAmount(BigDecimal.valueOf(850))
                .status("ACTIVE")
                .build();

        when(catalogService.getActiveCategories()).thenReturn(List.of(activeCat));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("home cleaning"))
                .andExpect(jsonPath("$[0].amount").value(1000))
                .andExpect(jsonPath("$[0].discount").value(15))
                .andExpect(jsonPath("$[0].finalAmount").value(850))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        SubCategoryResponseDTO activeSub = SubCategoryResponseDTO.builder()
                .id(1L)
                .categoryId(catId)
                .name("kitchen cleaning")
                .amount(BigDecimal.valueOf(1500))
                .finalAmount(BigDecimal.valueOf(1350))
                .status("ACTIVE")
                .build();

        when(catalogService.getActiveSubcategoriesByCategory(catId)).thenReturn(List.of(activeSub));

        mockMvc.perform(get("/api/categories/" + catId + "/subcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("kitchen cleaning"));

        VariantResponseDTO activeVar = VariantResponseDTO.builder()
                .id(100L)
                .subcategoryId(1L)
                .name("1 bhk")
                .amount(BigDecimal.valueOf(1500))
                .finalAmount(BigDecimal.valueOf(1350))
                .status("ACTIVE")
                .build();

        when(catalogService.getActiveVariantsBySubcategory(1L)).thenReturn(List.of(activeVar));

        mockMvc.perform(get("/api/subcategories/1/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("1 bhk"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
