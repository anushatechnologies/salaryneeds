package com.salaryneeds;

import com.salaryneeds.dto.catalog.*;
import com.salaryneeds.entity.CatalogServiceEntity;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.exception.*;
import com.salaryneeds.repository.CatalogServiceRepository;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.service.CatalogManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CatalogModuleServiceTest {

    @Mock
    private CatalogServiceRepository catalogServiceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @InjectMocks
    private CatalogManagementServiceImpl catalogManagementService;

    private UUID serviceId1;
    private UUID categoryId1;
    private CatalogServiceEntity topService1;
    private Category category1;
    private ServiceItem subCat1;

    @BeforeEach
    void setUp() {
        serviceId1 = UUID.randomUUID();
        categoryId1 = UUID.randomUUID();

        topService1 = CatalogServiceEntity.builder()
                .id(serviceId1)
                .name("home services")
                .description("Home maintenance and repair")
                .imageUrl("http://img.com/service.png")
                .displayOrder(1)
                .isActive(true)
                .build();

        category1 = Category.builder()
                .id(categoryId1)
                .service(topService1)
                .name("home cleaning")
                .description("Cleaning services")
                .iconUrl("http://img.com/clean.png")
                .displayOrder(1)
                .isActive(true)
                .build();

        subCat1 = ServiceItem.builder()
                .id(101L)
                .category(category1)
                .name("bathroom deep cleaning")
                .description("Intensive bathroom scrubbing")
                .basePrice(BigDecimal.valueOf(500))
                .durationMinutes(60)
                .isActive(true)
                .build();
    }

    // --- Level 1: Service Tests ---

    @Test
    @DisplayName("1. Create valid Service")
    void testCreateValidService() {
        ServiceRequestDTO request = ServiceRequestDTO.builder()
                .name("  Home   Services  ")
                .description("Full home services")
                .imageUrl("http://img.com/clean.png")
                .displayOrder(1)
                .status("ACTIVE")
                .build();

        when(catalogServiceRepository.existsByName("home services")).thenReturn(false);
        when(catalogServiceRepository.save(any(CatalogServiceEntity.class))).thenAnswer(invocation -> {
            CatalogServiceEntity srv = invocation.getArgument(0);
            srv.setId(UUID.randomUUID());
            return srv;
        });

        ServiceResponseDTO response = catalogManagementService.createService(request);

        assertNotNull(response);
        assertEquals("home services", response.getName());
        assertEquals("ACTIVE", response.getStatus());
        verify(catalogServiceRepository).save(any(CatalogServiceEntity.class));
    }

    @Test
    @DisplayName("2. Create duplicate Service throws 409")
    void testCreateDuplicateService() {
        ServiceRequestDTO request = ServiceRequestDTO.builder()
                .name("HOME SERVICES")
                .build();

        when(catalogServiceRepository.existsByName("home services")).thenReturn(true);

        assertThrows(DuplicateCatalogServiceException.class, () ->
                catalogManagementService.createService(request)
        );
    }

    // --- Level 2: Category Tests ---

    @Test
    @DisplayName("3. Create valid Category under Service")
    void testCreateValidCategory() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("AC Repair")
                .description("Air conditioning care")
                .build();

        when(catalogServiceRepository.findById(serviceId1)).thenReturn(Optional.of(topService1));
        when(categoryRepository.existsByServiceIdAndName(serviceId1, "ac repair")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CategoryResponseDTO response = catalogManagementService.createCategory(serviceId1, request);

        assertNotNull(response);
        assertEquals("ac repair", response.getName());
        assertEquals(serviceId1, response.getServiceId());
    }

    @Test
    @DisplayName("4. Create duplicate Category under same Service throws 409")
    void testCreateDuplicateCategory() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Home Cleaning")
                .build();

        when(catalogServiceRepository.findById(serviceId1)).thenReturn(Optional.of(topService1));
        when(categoryRepository.existsByServiceIdAndName(serviceId1, "home cleaning")).thenReturn(true);

        assertThrows(DuplicateCategoryException.class, () ->
                catalogManagementService.createCategory(serviceId1, request)
        );
    }

    // --- Level 3: Sub-Category Tests ---

    @Test
    @DisplayName("5. Create valid Sub-Category under Category")
    void testCreateValidSubCategory() {
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .name("Kitchen Deep Cleaning")
                .basePrice(BigDecimal.valueOf(800))
                .build();

        when(categoryRepository.findById(categoryId1)).thenReturn(Optional.of(category1));
        when(serviceItemRepository.existsByCategoryIdAndName(categoryId1, "kitchen deep cleaning")).thenReturn(false);
        when(serviceItemRepository.save(any(ServiceItem.class))).thenAnswer(inv -> {
            ServiceItem item = inv.getArgument(0);
            item.setId(201L);
            return item;
        });

        SubCategoryResponseDTO response = catalogManagementService.createSubCategory(categoryId1, request);

        assertNotNull(response);
        assertEquals("kitchen deep cleaning", response.getName());
        assertEquals(categoryId1, response.getCategoryId());
    }

    @Test
    @DisplayName("6. Create duplicate Sub-Category under same Category throws 409")
    void testCreateDuplicateSubCategory() {
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .name("Bathroom Deep Cleaning")
                .build();

        when(categoryRepository.findById(categoryId1)).thenReturn(Optional.of(category1));
        when(serviceItemRepository.existsByCategoryIdAndName(categoryId1, "bathroom deep cleaning")).thenReturn(true);

        assertThrows(DuplicateSubCategoryException.class, () ->
                catalogManagementService.createSubCategory(categoryId1, request)
        );
    }

    @Test
    @DisplayName("7. Get Sub-Categories by Category with filtering")
    void testGetSubCategoriesByCategory() {
        when(categoryRepository.existsById(categoryId1)).thenReturn(true);
        when(serviceItemRepository.findByCategoryIdAndStatus(categoryId1, true))
                .thenReturn(Collections.singletonList(subCat1));

        List<SubCategoryResponseDTO> results = catalogManagementService.getSubCategoriesByCategory(categoryId1, "ACTIVE");

        assertEquals(1, results.size());
        assertEquals("bathroom deep cleaning", results.get(0).getName());
    }
}
