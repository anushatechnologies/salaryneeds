package com.salaryneeds;

import com.salaryneeds.config.CorsConfig;
import com.salaryneeds.controller.AdminCatalogController;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;
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

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminCatalogController.class})
@Import({CorsConfig.class, AdminAuthInterceptor.class})
public class CorsAndToggleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogManagementService catalogManagementService;

    @MockBean
    private com.salaryneeds.service.FileStorageService fileStorageService;

    @Test
    @DisplayName("CORS Preflight: http://localhost:5174 is allowed and returns Access-Control-Allow-Origin")
    void testCorsPreflightAllowsLocalhost5174() throws Exception {
        mockMvc.perform(options("/api/admin/variants/sub-887960/toggle")
                        .header("Origin", "http://localhost:5174")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Toggle: PATCH /api/admin/variants/sub-887960/toggle returns 200 OK")
    void testVariantToggleWithPrefixedId() throws Exception {
        SubCategoryResponseDTO sub = SubCategoryResponseDTO.builder()
                .id(887960L)
                .name("plumbing")
                .status("ACTIVE")
                .isActive(true)
                .amount(BigDecimal.valueOf(500))
                .build();

        when(catalogManagementService.getSubCategoryById(887960L)).thenReturn(sub);
        when(catalogManagementService.updateSubCategoryStatus(887960L, "INACTIVE")).thenReturn(
                SubCategoryResponseDTO.builder()
                        .id(887960L)
                        .name("plumbing")
                        .status("INACTIVE")
                        .isActive(false)
                        .build()
        );

        mockMvc.perform(patch("/api/admin/variants/sub-887960/toggle")
                        .header("Origin", "http://localhost:5174")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
    }

    @Test
    @DisplayName("Status: PATCH /api/admin/variants/var-887960/status returns 200 OK")
    void testVariantStatusWithPrefixedId() throws Exception {
        VariantResponseDTO variant = VariantResponseDTO.builder()
                .id(887960L)
                .name("Standard")
                .status("INACTIVE")
                .isActive(false)
                .amount(BigDecimal.valueOf(300))
                .build();

        when(catalogManagementService.getVariantById(887960L)).thenReturn(variant);
        when(catalogManagementService.updateVariantStatus(887960L, "ACTIVE")).thenReturn(
                VariantResponseDTO.builder()
                        .id(887960L)
                        .name("Standard")
                        .status("ACTIVE")
                        .isActive(true)
                        .amount(BigDecimal.valueOf(300))
                        .build()
        );

        mockMvc.perform(patch("/api/admin/variants/var-887960/status")
                        .header("Origin", "http://localhost:5174")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
    }

    @Test
    @DisplayName("Fallback: Toggle unknown mock ID returns 200 OK fallback instead of crashing")
    void testVariantToggleUnknownMockIdFallback() throws Exception {
        mockMvc.perform(patch("/api/admin/variants/sub-unknown-99999/toggle")
                        .header("Origin", "http://localhost:5174")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
    }
}
