package com.salaryneeds;

import com.salaryneeds.dto.catalog.*;
import com.salaryneeds.entity.CatalogServiceEntity;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.entity.Variant;
import com.salaryneeds.exception.*;
import com.salaryneeds.repository.CatalogServiceRepository;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.repository.VariantRepository;
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

    @Mock
    private VariantRepository variantRepository;

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
                .name("kitchen cleaning")
                .description("Intensive kitchen scrubbing")
                .basePrice(BigDecimal.valueOf(1500))
                .discount(BigDecimal.valueOf(10))
                .finalAmount(BigDecimal.valueOf(1350))
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
    @DisplayName("3. Create Category stores name strictly in LOWERCASE regardless of input case")
    void testCategoryNameStoredInLowerCase() {
        CategoryRequestDTO requestUpper = CategoryRequestDTO.builder()
                .name("  HOME CLEANING SERVICES  ")
                .description("Complete residential cleaning")
                .imageUrl("http://example.com/clean.jpg")
                .status("ACTIVE")
                .build();

        when(categoryRepository.existsByName("home cleaning services")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CategoryResponseDTO response = catalogManagementService.createCategory(requestUpper);

        assertNotNull(response);
        assertEquals("home cleaning services", response.getName(), "Category name must be stored in lower case");
        assertEquals("ACTIVE", response.getStatus());
        verify(categoryRepository).save(argThat(cat -> "home cleaning services".equals(cat.getName())));
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

    // --- Level 3: Sub-Category / Service Tests ---

    @Test
    @DisplayName("5. Create Subcategory calculates final amount on backend and lowercases name")
    void testCreateValidSubCategoryWithFinalAmountCalculation() {
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .categoryId(categoryId1)
                .name("  KITCHEN CLEANING  ")
                .description("Complete kitchen deep cleaning")
                .amount(BigDecimal.valueOf(1500))
                .discount(BigDecimal.valueOf(10)) // 10% discount -> finalAmount = 1350
                .imageUrl("http://example.com/kitchen.jpg")
                .status("ACTIVE")
                .build();

        when(categoryRepository.findById(categoryId1)).thenReturn(Optional.of(category1));
        when(serviceItemRepository.existsByCategoryIdAndName(categoryId1, "kitchen cleaning")).thenReturn(false);
        when(serviceItemRepository.save(any(ServiceItem.class))).thenAnswer(inv -> {
            ServiceItem item = inv.getArgument(0);
            item.setId(101L);
            return item;
        });

        SubCategoryResponseDTO response = catalogManagementService.createSubCategory(request);

        assertNotNull(response);
        assertEquals("kitchen cleaning", response.getName(), "Subcategory name must be lowercased");
        assertEquals(0, BigDecimal.valueOf(1350).compareTo(response.getFinalAmount()), "Final amount must be backend calculated: 1500 - 10% = 1350");
        assertEquals(categoryId1, response.getCategoryId());
    }

    @Test
    @DisplayName("6. Create Subcategory with non-existing Category throws 404")
    void testCreateSubCategoryNonExistingCategoryThrows404() {
        UUID nonExistingCatId = UUID.randomUUID();
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .categoryId(nonExistingCatId)
                .name("Plumbing Repair")
                .amount(BigDecimal.valueOf(500))
                .build();

        when(categoryRepository.findById(nonExistingCatId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () ->
                catalogManagementService.createSubCategory(request)
        );
    }

    @Test
    @DisplayName("7. Create duplicate Sub-Category under same Category throws 409")
    void testCreateDuplicateSubCategory() {
        SubCategoryRequestDTO request = SubCategoryRequestDTO.builder()
                .name("Kitchen Cleaning")
                .build();

        when(categoryRepository.findById(categoryId1)).thenReturn(Optional.of(category1));
        when(serviceItemRepository.existsByCategoryIdAndName(categoryId1, "kitchen cleaning")).thenReturn(true);

        assertThrows(DuplicateSubCategoryException.class, () ->
                catalogManagementService.createSubCategory(categoryId1, request)
        );
    }

    // --- Level 4: Optional Variant Tests ---

    @Test
    @DisplayName("8. Create Optional Variant calculates backend final price and lowercases name")
    void testCreateValidVariant() {
        VariantRequestDTO request = VariantRequestDTO.builder()
                .name("  1.5 TON  ")
                .description("1.5 Ton AC repair")
                .amount(BigDecimal.valueOf(2000))
                .discount(BigDecimal.valueOf(10)) // 10% discount -> 1800
                .status("ACTIVE")
                .build();

        when(serviceItemRepository.findById(101L)).thenReturn(Optional.of(subCat1));
        when(variantRepository.existsBySubcategoryIdAndName(101L, "1.5 ton")).thenReturn(false);
        when(variantRepository.save(any(Variant.class))).thenAnswer(inv -> {
            Variant v = inv.getArgument(0);
            v.setId(501L);
            return v;
        });

        VariantResponseDTO response = catalogManagementService.createVariant(101L, request);

        assertNotNull(response);
        assertEquals("1.5 ton", response.getName(), "Variant name must be stored in lower case");
        assertEquals(0, BigDecimal.valueOf(1800).compareTo(response.getFinalAmount()), "Final price must be 1800 (2000 - 10%)");
        assertEquals(101L, response.getSubcategoryId());
    }

    @Test
    @DisplayName("9. Create Variant under non-existing Subcategory throws 404")
    void testCreateVariantNonExistingSubcategoryThrows404() {
        VariantRequestDTO request = VariantRequestDTO.builder()
                .name("2 Ton")
                .amount(BigDecimal.valueOf(2500))
                .build();

        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SubCategoryNotFoundException.class, () ->
                catalogManagementService.createVariant(999L, request)
        );
    }

    @Test
    @DisplayName("10. Variants are optional: Subcategory exists independently")
    void testSubcategoryWithZeroVariants() {
        when(serviceItemRepository.findById(101L)).thenReturn(Optional.of(subCat1));

        SubCategoryResponseDTO response = catalogManagementService.getSubCategoryById(101L, true);

        assertNotNull(response);
        assertEquals("kitchen cleaning", response.getName());
    }
}
