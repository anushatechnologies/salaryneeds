package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.AdminCatalogController;
import com.salaryneeds.controller.UserCatalogController;
import com.salaryneeds.controller.WorkerCatalogController;
import com.salaryneeds.dto.catalog.CategoryRequestDTO;
import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryRequestDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantRequestDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;
import com.salaryneeds.service.CatalogManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserAndWorkerCatalogTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CatalogManagementService catalogService;

    @Test
    @DisplayName("Admin creates Category -> Subcategory -> Variants; Both User and Worker read identical catalogue")
    void testUserAndWorkerReadSameActiveCatalogue() throws Exception {
        // 1. Admin creates active Category
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        CategoryRequestDTO catReq = CategoryRequestDTO.builder()
                .name("Home Cleaning " + uniqueSuffix)
                .description("Home cleaning services")
                .amount(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .imageUrl("https://example.com/home.jpg")
                .status("ACTIVE")
                .build();
        CategoryResponseDTO createdCat = catalogService.createCategory(catReq);
        UUID catId = createdCat.getId();

        // 2. Admin creates active Subcategory with variants
        SubCategoryRequestDTO subReq1 = SubCategoryRequestDTO.builder()
                .categoryId(catId)
                .name("Kitchen Cleaning " + uniqueSuffix)
                .description("Kitchen deep cleaning")
                .amount(BigDecimal.valueOf(1500))
                .discount(BigDecimal.valueOf(10))
                .imageUrl("https://example.com/kitchen.jpg")
                .status("ACTIVE")
                .build();
        SubCategoryResponseDTO createdSub1 = catalogService.createSubCategory(subReq1);
        Long subId1 = createdSub1.getId();

        // 3. Admin creates optional Variants under Subcategory 1
        VariantRequestDTO varReq1 = VariantRequestDTO.builder()
                .name("1 BHK")
                .description("Kitchen cleaning for 1 BHK")
                .amount(BigDecimal.valueOf(1500))
                .discount(BigDecimal.valueOf(10))
                .status("ACTIVE")
                .build();
        catalogService.createVariant(subId1, varReq1);

        VariantRequestDTO varReq2 = VariantRequestDTO.builder()
                .name("2 BHK")
                .description("Kitchen cleaning for 2 BHK")
                .amount(BigDecimal.valueOf(2000))
                .discount(BigDecimal.valueOf(10))
                .status("ACTIVE")
                .build();
        catalogService.createVariant(subId1, varReq2);

        // 4. Admin creates another Subcategory WITHOUT variants
        SubCategoryRequestDTO subReq2 = SubCategoryRequestDTO.builder()
                .categoryId(catId)
                .name("Bathroom Cleaning")
                .description("Bathroom deep cleaning")
                .amount(BigDecimal.valueOf(800))
                .discount(BigDecimal.valueOf(5))
                .status("ACTIVE")
                .build();
        SubCategoryResponseDTO createdSub2 = catalogService.createSubCategory(subReq2);
        Long subId2 = createdSub2.getId();

        // ==========================================
        // USER APIS VERIFICATION
        // ==========================================

        // User gets categories
        mockMvc.perform(get("/api/user/categories")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[?(@.id == '" + catId + "')].name").value("home cleaning " + uniqueSuffix));

        // User gets subcategories for Category
        mockMvc.perform(get("/api/user/categories/" + catId + "/subcategories")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[?(@.id == " + subId1 + ")].finalAmount").value(1350.0));

        // User gets single subcategory
        mockMvc.perform(get("/api/user/subcategories/" + subId1)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(subId1))
                .andExpect(jsonPath("$.data.name").value("kitchen cleaning " + uniqueSuffix))
                .andExpect(jsonPath("$.data.finalAmount").value(1350.0));

        // User gets variants for Subcategory 1 (2 BHK, 1 BHK)
        mockMvc.perform(get("/api/user/subcategories/" + subId1 + "/variants")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[?(@.name == '1 bhk')].finalAmount").value(1350.0))
                .andExpect(jsonPath("$.data[?(@.name == '2 bhk')].finalAmount").value(1800.0));

        // User gets variants for Subcategory 2 (NO VARIANTS -> empty list)
        mockMvc.perform(get("/api/user/subcategories/" + subId2 + "/variants")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        // ==========================================
        // WORKER APIS VERIFICATION (SAME DATA)
        // ==========================================

        // Worker gets categories
        mockMvc.perform(get("/api/worker/categories")
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[?(@.id == '" + catId + "')].name").value("home cleaning " + uniqueSuffix));

        // Worker gets subcategories for Category
        mockMvc.perform(get("/api/worker/categories/" + catId + "/subcategories")
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[?(@.id == " + subId1 + ")].finalAmount").value(1350.0));

        // Worker gets single subcategory
        mockMvc.perform(get("/api/worker/subcategories/" + subId1)
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(subId1))
                .andExpect(jsonPath("$.data.name").value("kitchen cleaning " + uniqueSuffix));

        // Worker gets variants for Subcategory 1
        mockMvc.perform(get("/api/worker/subcategories/" + subId1 + "/variants")
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));

        // Worker gets variants for Subcategory 2 (NO VARIANTS -> empty list)
        mockMvc.perform(get("/api/worker/subcategories/" + subId2 + "/variants")
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Inactive Category/Subcategory/Variant are excluded from User and Worker views")
    void testInactiveItemsExcludedFromUserAndWorker() throws Exception {
        // Create Inactive Category
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        CategoryRequestDTO catReq = CategoryRequestDTO.builder()
                .name("Inactive Category " + uniqueSuffix)
                .status("INACTIVE")
                .build();
        CategoryResponseDTO createdCat = catalogService.createCategory(catReq);

        // User should not see Inactive Category
        mockMvc.perform(get("/api/user/categories")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == '" + createdCat.getId() + "')]").doesNotExist());

        // Worker should not see Inactive Category
        mockMvc.perform(get("/api/worker/categories")
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == '" + createdCat.getId() + "')]").doesNotExist());

        // Calling subcategories on inactive category returns 404
        mockMvc.perform(get("/api/user/categories/" + createdCat.getId() + "/subcategories")
                        .header("X-Role", "USER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Open access checks across Admin, User, and Worker without JWT restrictions")
    void testRoleAuthorization() throws Exception {
        // User can access Admin APIs without JWT restrictions
        mockMvc.perform(get("/api/admin/services")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk());

        // Worker can access Admin APIs
        mockMvc.perform(get("/api/admin/services")
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk());

        // User can access Worker APIs
        mockMvc.perform(get("/api/worker/categories")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk());

        // Worker can access User APIs
        mockMvc.perform(get("/api/user/categories")
                        .header("X-Role", "WORKER"))
                .andExpect(status().isOk());

        // Unauthenticated request to User API -> 200 OK
        mockMvc.perform(get("/api/user/categories"))
                .andExpect(status().isOk());

        // Unauthenticated request to Worker API -> 200 OK
        mockMvc.perform(get("/api/worker/categories"))
                .andExpect(status().isOk());
    }
}
