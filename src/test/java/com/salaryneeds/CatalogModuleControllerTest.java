package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.AdminCatalogController;
import com.salaryneeds.dto.catalog.ServiceRequestDTO;
import com.salaryneeds.dto.catalog.ServiceResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryRequestDTO;
import com.salaryneeds.exception.CategoryNotFoundException;
import com.salaryneeds.exception.DuplicateCatalogServiceException;
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
        ServiceRequestDTO request = ServiceRequestDTO.builder()
                .name("Home Services")
                .description("Home maintenance")
                .build();

        ServiceResponseDTO response = ServiceResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("home services")
                .status("ACTIVE")
                .build();

        when(catalogManagementService.createService(any())).thenReturn(response);

        mockMvc.perform(post("/admin/services")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("home services"));
    }

    @Test
    @DisplayName("22. Open access allows service creation without JWT restriction before Firebase integration")
    void testUnauthorizedUserForbidden() throws Exception {
        ServiceRequestDTO request = ServiceRequestDTO.builder()
                .name("Home Services")
                .build();

        ServiceResponseDTO response = ServiceResponseDTO.builder()
                .id(java.util.UUID.randomUUID())
                .name("Home Services")
                .build();

        when(catalogManagementService.createService(any())).thenReturn(response);

        mockMvc.perform(post("/admin/services")
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("23. Open access allows unauthenticated request without JWT restriction before Firebase integration")
    void testUnauthenticatedRequestRejected() throws Exception {
        ServiceRequestDTO request = ServiceRequestDTO.builder()
                .name("Home Services")
                .build();

        ServiceResponseDTO response = ServiceResponseDTO.builder()
                .id(java.util.UUID.randomUUID())
                .name("Home Services")
                .build();

        when(catalogManagementService.createService(any())).thenReturn(response);

        mockMvc.perform(post("/admin/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Duplicate Service creation returns 409 Conflict")
    void testDuplicateServiceReturns409() throws Exception {
        ServiceRequestDTO request = ServiceRequestDTO.builder()
                .name("Home Services")
                .build();

        when(catalogManagementService.createService(any()))
                .thenThrow(new DuplicateCatalogServiceException("A Service with the name 'home services' already exists"));

        mockMvc.perform(post("/admin/services")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Service"));
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

        mockMvc.perform(post("/admin/categories/" + categoryId + "/sub-categories")
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

        mockMvc.perform(post("/admin/categories/" + nonExistingCatId + "/sub-categories")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category Not Found"));
    }

    @Test
    @DisplayName("Delete Service returns 204 No Content")
    void testDeleteServiceReturns204() throws Exception {
        UUID serviceId = UUID.randomUUID();
        doNothing().when(catalogManagementService).deleteService(serviceId);

        mockMvc.perform(delete("/admin/services/" + serviceId)
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }
}
