package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.AdminCatalogController;
import com.salaryneeds.dto.catalog.CategoryRequestDTO;
import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryRequestDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.exception.CategoryNotFoundException;
import com.salaryneeds.exception.DuplicateCategoryException;
import com.salaryneeds.exception.DuplicateSubCategoryException;
import com.salaryneeds.exception.GlobalExceptionHandler;
import com.salaryneeds.security.AdminAuthInterceptor;
import com.salaryneeds.service.CatalogManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AdminCatalogController.class})
@Import({GlobalExceptionHandler.class, AdminAuthInterceptor.class})
public class CatalogModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogManagementService catalogManagementService;

    @MockBean
    private com.salaryneeds.service.FileStorageService fileStorageService;

    @Test
    @DisplayName("21. Admin can access Admin Catalog APIs with ADMIN role")
    void testAdminAccessAllowed() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Home Cleaning")
                .description("Home maintenance")
                .build();

        CategoryResponseDTO response = CategoryResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("home cleaning")
                .status("ACTIVE")
                .build();

        when(catalogManagementService.createCategory(any())).thenReturn(response);

        mockMvc.perform(post("/admin/categories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("home cleaning"));
    }

    @Test
    @DisplayName("22. Unauthorized user (USER role) cannot access Admin Catalog APIs -> 403 Forbidden")
    void testUnauthorizedUserForbidden() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Home Cleaning")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("23. Unauthenticated request to Admin API is rejected -> 401 Unauthorized")
    void testUnauthenticatedRequestRejected() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Home Cleaning")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Duplicate Category creation returns 409 Conflict")
    void testDuplicateCategoryReturns409() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Home Cleaning")
                .build();

        when(catalogManagementService.createCategory(any()))
                .thenThrow(new DuplicateCategoryException("A Category with the name 'home cleaning' already exists"));

        mockMvc.perform(post("/admin/categories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Category"));
    }

    @Test
    @DisplayName("Duplicate Sub-Category creation returns 409 Conflict")
    void testDuplicateSubCategoryReturns409() throws Exception {
        UUID categoryId = UUID.randomUUID();
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .name("AC Servicing")
                .build();

        when(catalogManagementService.createSubCategory(eq(categoryId), any()))
                .thenThrow(new DuplicateSubCategoryException("A Sub-Category with the name 'ac servicing' already exists"));

        mockMvc.perform(post("/admin/categories/" + categoryId + "/subcategories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Sub-Category"));
    }

    @Test
    @DisplayName("Parent Category not found returns 404 Not Found")
    void testParentCategoryNotFoundReturns404() throws Exception {
        UUID nonExistingCatId = UUID.randomUUID();
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .name("AC Gas Refill")
                .build();

        when(catalogManagementService.createSubCategory(eq(nonExistingCatId), any()))
                .thenThrow(new CategoryNotFoundException("Category not found with ID: " + nonExistingCatId));

        mockMvc.perform(post("/admin/categories/" + nonExistingCatId + "/subcategories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category Not Found"));
    }

    @Test
    @DisplayName("Delete Category returns 204 No Content")
    void testDeleteCategoryReturns204() throws Exception {
        UUID categoryId = UUID.randomUUID();
        doNothing().when(catalogManagementService).deleteCategory(categoryId);

        mockMvc.perform(delete("/admin/categories/" + categoryId)
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }
}
