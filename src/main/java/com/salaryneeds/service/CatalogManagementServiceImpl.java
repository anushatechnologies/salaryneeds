package com.salaryneeds.service;

import com.salaryneeds.dto.catalog.*;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.entity.Variant;
import com.salaryneeds.exception.*;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.repository.VariantRepository;
import com.salaryneeds.util.CatalogNameNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogManagementServiceImpl implements CatalogManagementService {

    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final VariantRepository variantRepository;

    // ==========================================
    // 1. CATEGORY OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        CatalogNameNormalizer.validateCatalogName(request.getName(), "Category name", 255);
        CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 500);

        String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

        if (categoryRepository.existsByName(lowerCaseName)) {
            throw new DuplicateCategoryException("A Category with the name '" + lowerCaseName + "' already exists");
        }

        Boolean isActive = true;
        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            isActive = CatalogStatus.toBoolean(status);
        }

        Category category = Category.builder()
                .name(lowerCaseName)
                .description(request.getDescription())
                .iconUrl(request.getImageUrl())
                .isActive(isActive)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created standalone Category with ID: {} and Name: {}", saved.getId(), saved.getName());
        return mapToCategoryResponseDTO(saved, false);
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(UUID serviceId, CategoryRequestDTO request) {
        return createCategory(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(String search, String status) {
        return getCategoriesByService(null, search, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getCategoriesByService(UUID serviceId, String search, String status) {
        Boolean isActive = null;
        if (status != null && !status.trim().isEmpty()) {
            CatalogStatus catalogStatus = CatalogStatus.fromString(status);
            isActive = CatalogStatus.toBoolean(catalogStatus);
        }

        String cleanedSearch = null;
        if (search != null && !search.trim().isEmpty()) {
            cleanedSearch = CatalogNameNormalizer.toLowerCaseNormalized(search);
        }

        List<Category> categories;
        if (cleanedSearch != null || isActive != null) {
            categories = categoryRepository.searchCategories(cleanedSearch, isActive);
        } else {
            categories = categoryRepository.findAllByOrderByNameAsc();
        }

        return categories.stream()
                .map(cat -> mapToCategoryResponseDTO(cat, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UUID categoryId, boolean includeSubCategories) {
        if (categoryId == null) {
            throw new InvalidCatalogDataException("Category ID must not be null");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        return mapToCategoryResponseDTO(category, includeSubCategories);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO request) {
        if (categoryId == null) {
            throw new InvalidCatalogDataException("Category ID must not be null");
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        if (request.getName() != null) {
            CatalogNameNormalizer.validateCatalogName(request.getName(), "Category name", 255);
            String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

            if (categoryRepository.existsByNameAndIdNot(lowerCaseName, categoryId)) {
                throw new DuplicateCategoryException("A Category with the name '" + lowerCaseName + "' already exists");
            }

            category.setName(lowerCaseName);
        }

        if (request.getDescription() != null) {
            CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 500);
            category.setDescription(request.getDescription());
        }

        if (request.getImageUrl() != null) {
            category.setIconUrl(request.getImageUrl());
        }

        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            category.setIsActive(CatalogStatus.toBoolean(status));
        }

        Category saved = categoryRepository.save(category);
        log.info("Updated Category with ID: {}", saved.getId());
        return mapToCategoryResponseDTO(saved, false);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategoryStatus(UUID categoryId, String status) {
        if (categoryId == null) {
            throw new InvalidCatalogDataException("Category ID must not be null");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidCatalogDataException("Status must not be blank");
        }

        CatalogStatus catalogStatus = CatalogStatus.fromString(status);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        category.setIsActive(CatalogStatus.toBoolean(catalogStatus));
        Category saved = categoryRepository.save(category);
        log.info("Updated status of Category ID: {} to {}", saved.getId(), saved.getStatus());
        return mapToCategoryResponseDTO(saved, false);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        if (categoryId == null) {
            throw new InvalidCatalogDataException("Category ID must not be null");
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with ID: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
        log.info("Deleted Category with ID: {}", categoryId);
    }

    // ==========================================
    // 3. SUBCATEGORY / SERVICE (LEAF LEVEL)
    // ==========================================

    @Override
    @Transactional
    public SubCategoryResponseDTO createSubCategory(SubCategoryRequestDTO request) {
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }
        if (request.getCategoryId() == null) {
            throw new InvalidCatalogDataException("Category ID is required to create a Subcategory");
        }
        return createSubCategory(request.getCategoryId(), request);
    }

    @Override
    @Transactional
    public SubCategoryResponseDTO createSubCategory(UUID categoryId, SubCategoryRequestDTO request) {
        if (categoryId == null) {
            throw new InvalidCatalogDataException("Parent Category ID must not be null");
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        Category parentCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent Category not found with ID: " + categoryId));

        CatalogNameNormalizer.validateCatalogName(request.getName(), "Sub-Category name", 255);
        CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 2000);

        String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

        if (serviceItemRepository.existsByCategoryIdAndName(categoryId, lowerCaseName)) {
            throw new DuplicateSubCategoryException("A Sub-Category with the name '" + lowerCaseName +
                    "' already exists under parent Category '" + parentCategory.getName() + "'");
        }

        Boolean isActive = true;
        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            isActive = CatalogStatus.toBoolean(status);
        }

        BigDecimal basePrice = request.getAmount() != null ? request.getAmount() : (request.getBasePrice() != null ? request.getBasePrice() : BigDecimal.ZERO);
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;

        // Backend strictly calculates finalAmount
        BigDecimal finalAmount = calculateFinalAmount(basePrice, discount, request.getDiscountPrice());

        ServiceItem serviceItem = ServiceItem.builder()
                .category(parentCategory)
                .name(lowerCaseName)
                .description(request.getDescription())
                .basePrice(basePrice)
                .discount(discount)
                .discountPrice(finalAmount)
                .finalAmount(finalAmount)
                .imageUrl(request.getImageUrl())
                .isActive(isActive)
                .build();

        ServiceItem saved = serviceItemRepository.save(serviceItem);
        log.info("Created Sub-Category with ID: {} under parent Category: {}", saved.getId(), parentCategory.getName());
        return mapToSubCategoryResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryResponseDTO> getAllSubCategories(UUID categoryId, String search, String status) {
        if (categoryId != null) {
            return getSubCategoriesByCategory(categoryId, status);
        }

        Boolean isActive = null;
        if (status != null && !status.trim().isEmpty()) {
            CatalogStatus catalogStatus = CatalogStatus.fromString(status);
            isActive = CatalogStatus.toBoolean(catalogStatus);
        }

        List<ServiceItem> items;
        if (isActive != null) {
            items = serviceItemRepository.findByIsActiveTrue();
        } else {
            items = serviceItemRepository.findAll();
        }

        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = CatalogNameNormalizer.toLowerCaseNormalized(search);
            items = items.stream()
                    .filter(s -> s.getName().contains(lowerSearch) ||
                            (s.getDescription() != null && s.getDescription().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }

        return items.stream()
                .map(this::mapToSubCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryResponseDTO> getSubCategoriesByCategory(UUID categoryId, String status) {
        if (categoryId == null) {
            throw new InvalidCatalogDataException("Parent Category ID must not be null");
        }

        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Parent Category not found with ID: " + categoryId);
        }

        Boolean isActive = null;
        if (status != null && !status.trim().isEmpty()) {
            CatalogStatus catalogStatus = CatalogStatus.fromString(status);
            isActive = CatalogStatus.toBoolean(catalogStatus);
        }

        List<ServiceItem> items = serviceItemRepository.findByCategoryIdAndStatus(categoryId, isActive);
        return items.stream()
                .map(this::mapToSubCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubCategoryResponseDTO getSubCategoryById(Long subCategoryId) {
        return getSubCategoryById(subCategoryId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public SubCategoryResponseDTO getSubCategoryById(Long subCategoryId, boolean includeVariants) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }

        ServiceItem item = serviceItemRepository.findById(subCategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Sub-Category not found with ID: " + subCategoryId));

        return mapToSubCategoryResponseDTO(item);
    }

    @Override
    @Transactional
    public SubCategoryResponseDTO updateSubCategory(Long subCategoryId, SubCategoryRequestDTO request) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        ServiceItem serviceItem = serviceItemRepository.findById(subCategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Sub-Category not found with ID: " + subCategoryId));

        Category targetParent = serviceItem.getCategory();
        if (request.getCategoryId() != null && !request.getCategoryId().equals(targetParent.getId())) {
            targetParent = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Target Parent Category not found with ID: " + request.getCategoryId()));
            serviceItem.setCategory(targetParent);
        }

        if (request.getName() != null) {
            CatalogNameNormalizer.validateCatalogName(request.getName(), "Sub-Category name", 255);
            String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

            if (serviceItemRepository.existsByCategoryIdAndNameAndIdNot(targetParent.getId(), lowerCaseName, subCategoryId)) {
                throw new DuplicateSubCategoryException("A Sub-Category with the name '" + lowerCaseName +
                        "' already exists under parent Category '" + targetParent.getName() + "'");
            }

            serviceItem.setName(lowerCaseName);
        }

        if (request.getDescription() != null) {
            CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 2000);
            serviceItem.setDescription(request.getDescription());
        }

        if (request.getAmount() != null) {
            serviceItem.setBasePrice(request.getAmount());
        }

        if (request.getDiscount() != null) {
            serviceItem.setDiscount(request.getDiscount());
        }

        serviceItem.calculateFinalAmount();

        if (request.getImageUrl() != null) {
            serviceItem.setImageUrl(request.getImageUrl());
        }

        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            serviceItem.setIsActive(CatalogStatus.toBoolean(status));
        }

        ServiceItem saved = serviceItemRepository.save(serviceItem);
        log.info("Updated Sub-Category with ID: {}", saved.getId());
        return mapToSubCategoryResponseDTO(saved);
    }

    @Override
    @Transactional
    public SubCategoryResponseDTO updateSubCategoryStatus(Long subCategoryId, String status) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidCatalogDataException("Status must not be blank");
        }

        CatalogStatus catalogStatus = CatalogStatus.fromString(status);
        ServiceItem item = serviceItemRepository.findById(subCategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Sub-Category not found with ID: " + subCategoryId));

        item.setIsActive(CatalogStatus.toBoolean(catalogStatus));
        ServiceItem saved = serviceItemRepository.save(item);
        log.info("Updated status of Sub-Category ID: {} to {}", saved.getId(), saved.getStatus());
        return mapToSubCategoryResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteSubCategory(Long subCategoryId) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }
        if (!serviceItemRepository.existsById(subCategoryId)) {
            throw new SubCategoryNotFoundException("Sub-Category not found with ID: " + subCategoryId);
        }
        serviceItemRepository.deleteById(subCategoryId);
        log.info("Deleted Sub-Category with ID: {}", subCategoryId);
    }

    // ==========================================
    // 4. OPTIONAL VARIANT OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public VariantResponseDTO createVariant(Long subCategoryId, VariantRequestDTO request) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        ServiceItem parentSubCategory = serviceItemRepository.findById(subCategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Sub-Category not found with ID: " + subCategoryId));

        CatalogNameNormalizer.validateCatalogName(request.getName(), "Variant name", 255);
        CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 1000);

        String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

        if (variantRepository.existsBySubcategoryIdAndName(subCategoryId, lowerCaseName)) {
            throw new DuplicateVariantException("A Variant with the name '" + lowerCaseName +
                    "' already exists under parent Sub-Category '" + parentSubCategory.getName() + "'");
        }

        Boolean isActive = true;
        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            isActive = CatalogStatus.toBoolean(status);
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;

        // Backend strictly calculates finalAmount
        BigDecimal finalAmount = calculateFinalAmount(amount, discount, null);

        Variant variant = Variant.builder()
                .subcategory(parentSubCategory)
                .name(lowerCaseName)
                .description(request.getDescription())
                .amount(amount)
                .discount(discount)
                .finalAmount(finalAmount)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .imageUrl(request.getImageUrl())
                .status(isActive ? "ACTIVE" : "INACTIVE")
                .isActive(isActive)
                .build();

        Variant saved = variantRepository.save(variant);
        log.info("Created Variant with ID: {} under parent Sub-Category: {}", saved.getId(), parentSubCategory.getName());
        return mapToVariantResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponseDTO> getVariantsBySubCategory(Long subCategoryId, String status) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }

        if (!serviceItemRepository.existsById(subCategoryId)) {
            throw new SubCategoryNotFoundException("Sub-Category not found with ID: " + subCategoryId);
        }

        Boolean isActive = null;
        if (status != null && !status.trim().isEmpty()) {
            CatalogStatus catalogStatus = CatalogStatus.fromString(status);
            isActive = CatalogStatus.toBoolean(catalogStatus);
        }

        List<Variant> variants = variantRepository.findBySubcategoryIdAndStatus(subCategoryId, isActive);
        return variants.stream()
                .map(this::mapToVariantResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponseDTO getVariantById(Long variantId) {
        if (variantId == null || variantId <= 0) {
            throw new InvalidCatalogDataException("Variant ID must be a valid positive number");
        }

        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Variant not found with ID: " + variantId));

        return mapToVariantResponseDTO(variant);
    }

    @Override
    @Transactional
    public VariantResponseDTO updateVariant(Long variantId, VariantRequestDTO request) {
        if (variantId == null || variantId <= 0) {
            throw new InvalidCatalogDataException("Variant ID must be a valid positive number");
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Variant not found with ID: " + variantId));

        if (request.getName() != null) {
            CatalogNameNormalizer.validateCatalogName(request.getName(), "Variant name", 255);
            String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

            if (variantRepository.existsBySubcategoryIdAndNameAndIdNot(variant.getSubcategory().getId(), lowerCaseName, variantId)) {
                throw new DuplicateVariantException("A Variant with the name '" + lowerCaseName +
                        "' already exists under parent Sub-Category '" + variant.getSubcategory().getName() + "'");
            }

            variant.setName(lowerCaseName);
        }

        if (request.getDescription() != null) {
            CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 1000);
            variant.setDescription(request.getDescription());
        }

        if (request.getAmount() != null) {
            variant.setAmount(request.getAmount());
        }

        if (request.getDiscount() != null) {
            variant.setDiscount(request.getDiscount());
        }

        variant.calculateFinalAmount();

        if (request.getDisplayOrder() != null) {
            variant.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getImageUrl() != null) {
            variant.setImageUrl(request.getImageUrl());
        }

        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            variant.setStatus(status.name());
            variant.setIsActive(CatalogStatus.toBoolean(status));
        }

        Variant saved = variantRepository.save(variant);
        log.info("Updated Variant with ID: {}", saved.getId());
        return mapToVariantResponseDTO(saved);
    }

    @Override
    @Transactional
    public VariantResponseDTO updateVariantStatus(Long variantId, String status) {
        if (variantId == null || variantId <= 0) {
            throw new InvalidCatalogDataException("Variant ID must be a valid positive number");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidCatalogDataException("Status must not be blank");
        }

        CatalogStatus catalogStatus = CatalogStatus.fromString(status);
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Variant not found with ID: " + variantId));

        variant.setStatus(catalogStatus.name());
        variant.setIsActive(CatalogStatus.toBoolean(catalogStatus));
        Variant saved = variantRepository.save(variant);
        log.info("Updated status of Variant ID: {} to {}", saved.getId(), saved.getStatus());
        return mapToVariantResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        if (variantId == null || variantId <= 0) {
            throw new InvalidCatalogDataException("Variant ID must be a valid positive number");
        }
        if (!variantRepository.existsById(variantId)) {
            throw new VariantNotFoundException("Variant not found with ID: " + variantId);
        }
        variantRepository.deleteById(variantId);
        log.info("Deleted Variant with ID: {}", variantId);
    }

    // ==========================================
    // 5. USER & WORKER ACTIVE CATALOG RETRIEVAL
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveOrderByDisplayOrderAscNameAsc(true);
        return categories.stream()
                .map(c -> mapToCategoryResponseDTO(c, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryResponseDTO> getActiveSubCategoriesByCategory(UUID categoryId) {
        if (categoryId == null) {
            throw new InvalidCatalogDataException("Category ID must not be null");
        }

        categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Active Category not found with ID: " + categoryId));

        List<ServiceItem> items = serviceItemRepository.findByCategoryIdAndStatus(categoryId, true);
        return items.stream()
                .map(this::mapToSubCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubCategoryResponseDTO getActiveSubCategoryById(Long subCategoryId) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }

        ServiceItem item = serviceItemRepository.findByIdAndIsActiveTrue(subCategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Active Sub-Category not found with ID: " + subCategoryId));

        if (item.getCategory() == null || !Boolean.TRUE.equals(item.getCategory().getIsActive())) {
            throw new SubCategoryNotFoundException("Active parent Category not found for Sub-Category ID: " + subCategoryId);
        }

        return mapToSubCategoryResponseDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponseDTO> getActiveVariantsBySubCategory(Long subCategoryId) {
        if (subCategoryId == null || subCategoryId <= 0) {
            throw new InvalidCatalogDataException("Sub-Category ID must be a valid positive number");
        }

        ServiceItem item = serviceItemRepository.findByIdAndIsActiveTrue(subCategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Active Sub-Category not found with ID: " + subCategoryId));

        if (item.getCategory() == null || !Boolean.TRUE.equals(item.getCategory().getIsActive())) {
            throw new SubCategoryNotFoundException("Active parent Category not found for Sub-Category ID: " + subCategoryId);
        }

        List<Variant> variants = variantRepository.findBySubcategoryIdAndStatus(subCategoryId, true);
        return variants.stream()
                .map(this::mapToVariantResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 5. DATA RESET OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public void clearAllCatalogData() {
        variantRepository.deleteAll();
        serviceItemRepository.deleteAll();
        categoryRepository.deleteAll();
        log.info("Cleared all catalog data (variants, subcategories, categories) from database.");
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private BigDecimal calculateFinalAmount(BigDecimal basePrice, BigDecimal discount, BigDecimal fallback) {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return (fallback != null && fallback.compareTo(BigDecimal.ZERO) > 0) ? fallback : basePrice;
        }
        if (discount.compareTo(BigDecimal.valueOf(100)) <= 0) {
            // Percentage discount
            BigDecimal discountAmt = basePrice.multiply(discount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return basePrice.subtract(discountAmt).max(BigDecimal.ZERO);
        } else {
            // Flat discount
            return basePrice.subtract(discount).max(BigDecimal.ZERO);
        }
    }

    private CategoryResponseDTO mapToCategoryResponseDTO(Category category, boolean includeSubCategories) {
        int subCatsCount = serviceItemRepository.countByCategoryId(category.getId());
        List<SubCategoryResponseDTO> subCats = null;

        if (includeSubCategories) {
            subCats = serviceItemRepository.findByCategoryId(category.getId())
                    .stream()
                    .map(this::mapToSubCategoryResponseDTO)
                    .collect(Collectors.toList());
        }

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getIconUrl())
                .image(category.getIconUrl())
                .status(category.getStatus())
                .isActive(category.getIsActive())
                .subCategoriesCount(subCatsCount)
                .subCategories(subCats)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private SubCategoryResponseDTO mapToSubCategoryResponseDTO(ServiceItem item) {
        Category cat = item.getCategory();

        String catName = cat != null ? cat.getName() : null;
        UUID catId = cat != null ? cat.getId() : null;

        return SubCategoryResponseDTO.builder()
                .id(item.getId())
                .parentCategory(catName)
                .parentCategoryId(catId)
                .categoryId(catId)
                .categoryName(catName)
                .subCategoryName(item.getName())
                .name(item.getName())
                .description(item.getDescription())
                .amount(item.getBasePrice())
                .basePrice(item.getBasePrice())
                .discount(item.getDiscount())
                .discountPrice(item.getFinalAmount())
                .finalPrice(item.getFinalAmount())
                .finalAmount(item.getFinalAmount())
                .uploadImage(item.getImageUrl())
                .imageUrl(item.getImageUrl())
                .image(item.getImageUrl())
                .status(item.getStatus())
                .isActive(item.getIsActive())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private VariantResponseDTO mapToVariantResponseDTO(Variant variant) {
        ServiceItem sub = variant.getSubcategory();
        Category cat = (sub != null) ? sub.getCategory() : null;

        String catName = (cat != null) ? cat.getName() : null;
        UUID catId = (cat != null) ? cat.getId() : null;
        String subName = (sub != null) ? sub.getName() : null;
        Long subId = (sub != null) ? sub.getId() : null;

        return VariantResponseDTO.builder()
                .id(variant.getId())
                .parentCategory(catName)
                .parentCategoryId(catId)
                .categoryId(catId)
                .categoryName(catName)
                .parentSubCategory(subName)
                .parentSubCategoryId(subId)
                .subcategoryId(subId)
                .subcategoryName(subName)
                .variantName(variant.getName())
                .name(variant.getName())
                .description(variant.getDescription())
                .basePrice(variant.getAmount())
                .amount(variant.getAmount())
                .discount(variant.getDiscount())
                .finalPrice(variant.getFinalAmount())
                .finalAmount(variant.getFinalAmount())
                .displayOrder(variant.getDisplayOrder())
                .variantImage(variant.getImageUrl())
                .imageUrl(variant.getImageUrl())
                .image(variant.getImageUrl())
                .status(variant.getStatus())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}
