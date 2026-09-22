package com.salaryneeds.service;

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

    private final CatalogServiceRepository catalogServiceRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final VariantRepository variantRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.salaryneeds.service.storage.SupabaseStorageService supabaseStorageService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private FileStorageService fileStorageService;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = createObjectMapper();

    private static com.fasterxml.jackson.databind.ObjectMapper createObjectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private String processCatalogImageUrl(String imageUrl, String folder) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }
        if (imageUrl.contains("/storage/v1/object/public/") || imageUrl.contains("/storage/v1/s3")) {
            return imageUrl;
        }
        if (fileStorageService != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            try {
                String s3Url = fileStorageService.storeFromUrl(imageUrl, folder);
                if (s3Url != null && !s3Url.isBlank()) {
                    return s3Url;
                }
            } catch (Exception e) {
                log.warn("Notice: could not store remote image to S3 for folder {}: {}", folder, e.getMessage());
            }
        }
        return imageUrl;
    }

    private void syncCategoryJsonToS3(Category category) {
        if (supabaseStorageService == null || category == null || category.getId() == null) {
            return;
        }
        try {
            CategoryResponseDTO dto = mapToCategoryResponseDTO(category, true);
            byte[] bytes = objectMapper.writeValueAsBytes(dto);
            supabaseStorageService.uploadCatalogJson("categories/" + category.getId() + "/category.json", bytes);
            log.info("Synced Category JSON to S3: categories/{}/category.json", category.getId());
        } catch (Exception e) {
            log.warn("Notice: could not sync category JSON to S3: {}", e.getMessage());
        }
    }

    private void syncSubCategoryJsonToS3(ServiceItem item) {
        if (supabaseStorageService == null || item == null || item.getId() == null) {
            return;
        }
        try {
            SubCategoryResponseDTO dto = mapToSubCategoryResponseDTO(item);
            byte[] bytes = objectMapper.writeValueAsBytes(dto);
            supabaseStorageService.uploadCatalogJson("subcategories/" + item.getId() + "/subcategory.json", bytes);
            log.info("Synced SubCategory JSON to S3: subcategories/{}/subcategory.json", item.getId());
        } catch (Exception e) {
            log.warn("Notice: could not sync subcategory JSON to S3: {}", e.getMessage());
        }
    }

    private void syncVariantJsonToS3(Variant variant) {
        if (supabaseStorageService == null || variant == null || variant.getId() == null) {
            return;
        }
        try {
            VariantResponseDTO dto = mapToVariantResponseDTO(variant);
            byte[] bytes = objectMapper.writeValueAsBytes(dto);
            supabaseStorageService.uploadCatalogJson("variants/" + variant.getId() + "/variant.json", bytes);
            log.info("Synced Variant JSON to S3: variants/{}/variant.json", variant.getId());
        } catch (Exception e) {
            log.warn("Notice: could not sync variant JSON to S3: {}", e.getMessage());
        }
    }

    // ==========================================
    // 1. SERVICE (TOP LEVEL)
    // ==========================================

    @Override
    @Transactional
    public ServiceResponseDTO createService(ServiceRequestDTO request) {
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        CatalogNameNormalizer.validateCatalogName(request.getName(), "Service name", 100);
        CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 500);

        String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

        if (catalogServiceRepository.existsByName(lowerCaseName)) {
            throw new DuplicateCatalogServiceException("A Service with the name '" + lowerCaseName + "' already exists");
        }

        Boolean isActive = true;
        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            isActive = CatalogStatus.toBoolean(status);
        }

        CatalogServiceEntity entity = CatalogServiceEntity.builder()
                .name(lowerCaseName)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(isActive)
                .build();

        CatalogServiceEntity saved = catalogServiceRepository.save(entity);
        log.info("Created Service with ID: {} and Name: {}", saved.getId(), saved.getName());
        return mapToServiceResponseDTO(saved, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> getAllServices(String search, String status) {
        Boolean isActive = null;
        if (status != null && !status.trim().isEmpty()) {
            CatalogStatus catalogStatus = CatalogStatus.fromString(status);
            isActive = CatalogStatus.toBoolean(catalogStatus);
        }

        String cleanedSearch = null;
        if (search != null && !search.trim().isEmpty()) {
            cleanedSearch = CatalogNameNormalizer.toLowerCaseNormalized(search);
        }

        List<CatalogServiceEntity> entities;
        if (cleanedSearch != null || isActive != null) {
            entities = catalogServiceRepository.searchServices(cleanedSearch, isActive);
        } else {
            entities = catalogServiceRepository.findAllByOrderByDisplayOrderAscNameAsc();
        }

        return entities.stream()
                .map(srv -> mapToServiceResponseDTO(srv, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponseDTO getServiceById(UUID serviceId, boolean includeCategories) {
        if (serviceId == null) {
            throw new InvalidCatalogDataException("Service ID must not be null");
        }

        CatalogServiceEntity entity = catalogServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CatalogServiceNotFoundException("Service not found with ID: " + serviceId));

        return mapToServiceResponseDTO(entity, includeCategories);
    }

    @Override
    @Transactional
    public ServiceResponseDTO updateService(UUID serviceId, ServiceRequestDTO request) {
        if (serviceId == null) {
            throw new InvalidCatalogDataException("Service ID must not be null");
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        CatalogServiceEntity entity = catalogServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CatalogServiceNotFoundException("Service not found with ID: " + serviceId));

        if (request.getName() != null) {
            CatalogNameNormalizer.validateCatalogName(request.getName(), "Service name", 100);
            String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

            if (catalogServiceRepository.existsByNameAndIdNot(lowerCaseName, serviceId)) {
                throw new DuplicateCatalogServiceException("A Service with the name '" + lowerCaseName + "' already exists");
            }

            entity.setName(lowerCaseName);
        }

        if (request.getDescription() != null) {
            CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 500);
            entity.setDescription(request.getDescription());
        }

        if (request.getImageUrl() != null) {
            entity.setImageUrl(request.getImageUrl());
        }

        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            entity.setIsActive(CatalogStatus.toBoolean(status));
        }

        CatalogServiceEntity saved = catalogServiceRepository.save(entity);
        log.info("Updated Service with ID: {}", saved.getId());
        return mapToServiceResponseDTO(saved, false);
    }

    @Override
    @Transactional
    public ServiceResponseDTO updateServiceStatus(UUID serviceId, String status) {
        if (serviceId == null) {
            throw new InvalidCatalogDataException("Service ID must not be null");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidCatalogDataException("Status must not be blank");
        }

        CatalogStatus catalogStatus = CatalogStatus.fromString(status);
        CatalogServiceEntity entity = catalogServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CatalogServiceNotFoundException("Service not found with ID: " + serviceId));

        entity.setIsActive(CatalogStatus.toBoolean(catalogStatus));
        CatalogServiceEntity saved = catalogServiceRepository.save(entity);
        log.info("Updated status of Service ID: {} to {}", saved.getId(), saved.getStatus());
        return mapToServiceResponseDTO(saved, false);
    }

    @Override
    @Transactional
    public void deleteService(UUID serviceId) {
        if (serviceId == null) {
            throw new InvalidCatalogDataException("Service ID must not be null");
        }
        if (!catalogServiceRepository.existsById(serviceId)) {
            throw new CatalogServiceNotFoundException("Service not found with ID: " + serviceId);
        }
        catalogServiceRepository.deleteById(serviceId);
        log.info("Deleted Service with ID: {}", serviceId);
    }

    // ==========================================
    // 2. CATEGORY OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }
        if (request.getServiceId() != null) {
            return createCategory(request.getServiceId(), request);
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

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalAmount = calculateFinalAmount(amount, discount, request.getFinalAmount());

        String iconUrl = processCatalogImageUrl(request.getImageUrl(), "categories");
        Category category = Category.builder()
                .name(lowerCaseName)
                .description(request.getDescription())
                .amount(amount)
                .discount(discount)
                .finalAmount(finalAmount)
                .iconUrl(iconUrl)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(isActive)
                .build();

        Category saved = categoryRepository.save(category);
        syncCategoryJsonToS3(saved);
        log.info("Created standalone Category with ID: {} and Name: {}", saved.getId(), saved.getName());
        return mapToCategoryResponseDTO(saved, false);
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(UUID serviceId, CategoryRequestDTO request) {
        if (serviceId == null) {
            return createCategory(request);
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        CatalogServiceEntity parentService = catalogServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CatalogServiceNotFoundException("Parent Service not found with ID: " + serviceId));

        CatalogNameNormalizer.validateCatalogName(request.getName(), "Category name", 255);
        CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 500);

        String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

        if (categoryRepository.existsByServiceIdAndName(serviceId, lowerCaseName)) {
            throw new DuplicateCategoryException("A Category with the name '" + lowerCaseName +
                    "' already exists under parent Service '" + parentService.getName() + "'");
        }

        Boolean isActive = true;
        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            isActive = CatalogStatus.toBoolean(status);
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalAmount = calculateFinalAmount(amount, discount, request.getFinalAmount());

        String iconUrl = processCatalogImageUrl(request.getImageUrl(), "categories");
        Category category = Category.builder()
                .service(parentService)
                .name(lowerCaseName)
                .description(request.getDescription())
                .amount(amount)
                .discount(discount)
                .finalAmount(finalAmount)
                .iconUrl(iconUrl)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(isActive)
                .build();

        Category saved = categoryRepository.save(category);
        syncCategoryJsonToS3(saved);
        log.info("Created Category with ID: {} under Service: {}", saved.getId(), parentService.getName());
        return mapToCategoryResponseDTO(saved, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(String search, String status) {
        return getCategoriesByService(null, search, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getCategoriesByService(UUID serviceId, String search, String status) {
        if (serviceId != null && !catalogServiceRepository.existsById(serviceId)) {
            throw new CatalogServiceNotFoundException("Parent Service not found with ID: " + serviceId);
        }

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
        if (cleanedSearch != null || isActive != null || serviceId != null) {
            categories = categoryRepository.searchCategories(serviceId, cleanedSearch, isActive);
        } else {
            categories = categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
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

        CatalogServiceEntity targetService = category.getService();
        if (request.getServiceId() != null && (targetService == null || !request.getServiceId().equals(targetService.getId()))) {
            targetService = catalogServiceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new CatalogServiceNotFoundException("Target Parent Service not found with ID: " + request.getServiceId()));
            category.setService(targetService);
        }

        if (request.getName() != null) {
            CatalogNameNormalizer.validateCatalogName(request.getName(), "Category name", 255);
            String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

            UUID targetServiceId = targetService != null ? targetService.getId() : null;
            if (targetServiceId != null && categoryRepository.existsByServiceIdAndNameAndIdNot(targetServiceId, lowerCaseName, categoryId)) {
                throw new DuplicateCategoryException("A Category with the name '" + lowerCaseName +
                        "' already exists under parent Service '" + targetService.getName() + "'");
            } else if (targetServiceId == null && categoryRepository.existsByNameAndIdNot(lowerCaseName, categoryId)) {
                throw new DuplicateCategoryException("A Category with the name '" + lowerCaseName + "' already exists");
            }

            category.setName(lowerCaseName);
        }

        if (request.getDescription() != null) {
            CatalogNameNormalizer.validateOptionalText(request.getDescription(), "Description", 500);
            category.setDescription(request.getDescription());
        }

        if (request.getAmount() != null) {
            category.setAmount(request.getAmount());
        }

        if (request.getDiscount() != null) {
            category.setDiscount(request.getDiscount());
        }

        category.calculateFinalAmount();

        if (request.getImageUrl() != null) {
            category.setIconUrl(processCatalogImageUrl(request.getImageUrl(), "categories"));
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            category.setIsActive(CatalogStatus.toBoolean(status));
        }

        Category saved = categoryRepository.save(category);
        syncCategoryJsonToS3(saved);
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
        syncCategoryJsonToS3(saved);
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

        String itemImage = processCatalogImageUrl(request.getImageUrl(), "subcategories");
        ServiceItem serviceItem = ServiceItem.builder()
                .category(parentCategory)
                .name(lowerCaseName)
                .description(request.getDescription())
                .basePrice(basePrice)
                .discount(discount)
                .discountPrice(finalAmount)
                .finalAmount(finalAmount)
                .imageUrl(itemImage)
                .isActive(isActive)
                .build();

        ServiceItem saved = serviceItemRepository.save(serviceItem);
        syncSubCategoryJsonToS3(saved);
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
            serviceItem.setImageUrl(processCatalogImageUrl(request.getImageUrl(), "subcategories"));
        }

        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            serviceItem.setIsActive(CatalogStatus.toBoolean(status));
        }

        ServiceItem saved = serviceItemRepository.save(serviceItem);
        syncSubCategoryJsonToS3(saved);
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
        syncSubCategoryJsonToS3(saved);
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

        String varImage = processCatalogImageUrl(request.getImageUrl(), "variants");
        Variant variant = Variant.builder()
                .subcategory(parentSubCategory)
                .name(lowerCaseName)
                .description(request.getDescription())
                .amount(amount)
                .discount(discount)
                .finalAmount(finalAmount)
                .imageUrl(varImage)
                .status(isActive ? "ACTIVE" : "INACTIVE")
                .isActive(isActive)
                .build();

        Variant saved = variantRepository.save(variant);
        syncVariantJsonToS3(saved);
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

        if (request.getImageUrl() != null) {
            variant.setImageUrl(processCatalogImageUrl(request.getImageUrl(), "variants"));
        }

        if (request.getStatus() != null) {
            CatalogStatus status = CatalogStatus.fromString(request.getStatus());
            variant.setStatus(status.name());
            variant.setIsActive(CatalogStatus.toBoolean(status));
        }

        Variant saved = variantRepository.save(variant);
        syncVariantJsonToS3(saved);
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
        syncVariantJsonToS3(saved);
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

    private ServiceResponseDTO mapToServiceResponseDTO(CatalogServiceEntity entity, boolean includeCategories) {
        int categoriesCount = categoryRepository.countByServiceId(entity.getId());
        List<CategoryResponseDTO> cats = null;

        if (includeCategories) {
            cats = categoryRepository.findByServiceId(entity.getId())
                    .stream()
                    .map(c -> mapToCategoryResponseDTO(c, true))
                    .collect(Collectors.toList());
        }

        return ServiceResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .displayOrder(entity.getDisplayOrder())
                .status(entity.getStatus())
                .categoriesCount(categoriesCount)
                .categories(cats)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
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
                .serviceId(category.getService() != null ? category.getService().getId() : null)
                .serviceName(category.getService() != null ? category.getService().getName() : null)
                .name(category.getName())
                .description(category.getDescription())
                .amount(category.getAmount() != null ? category.getAmount() : BigDecimal.ZERO)
                .discount(category.getDiscount() != null ? category.getDiscount() : BigDecimal.ZERO)
                .finalAmount(category.getFinalAmount() != null ? category.getFinalAmount() : BigDecimal.ZERO)
                .imageUrl(category.getIconUrl())
                .image(category.getIconUrl())
                .displayOrder(category.getDisplayOrder())
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

        return SubCategoryResponseDTO.builder()
                .id(item.getId())
                .categoryId(cat != null ? cat.getId() : null)
                .categoryName(cat != null ? cat.getName() : null)
                .name(item.getName())
                .description(item.getDescription())
                .amount(item.getBasePrice())
                .basePrice(item.getBasePrice())
                .discount(item.getDiscount())
                .discountPrice(item.getFinalAmount())
                .finalAmount(item.getFinalAmount())
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

        return VariantResponseDTO.builder()
                .id(variant.getId())
                .subcategoryId(sub != null ? sub.getId() : null)
                .subcategoryName(sub != null ? sub.getName() : null)
                .categoryId(cat != null ? cat.getId() : null)
                .categoryName(cat != null ? cat.getName() : null)
                .name(variant.getName())
                .description(variant.getDescription())
                .amount(variant.getAmount())
                .discount(variant.getDiscount())
                .finalAmount(variant.getFinalAmount())
                .imageUrl(variant.getImageUrl())
                .image(variant.getImageUrl())
                .status(variant.getStatus())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void clearAllCatalogData() {
        log.info("Purging all catalog data (variants, subcategories, categories, services)...");
        variantRepository.deleteAll();
        serviceItemRepository.deleteAll();
        categoryRepository.deleteAll();
        catalogServiceRepository.deleteAll();
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> syncAllCatalogToS3() {
        log.info("Initiating full catalog sync to S3 storage...");
        int catCount = 0;
        int subCount = 0;
        int varCount = 0;

        List<Category> categories = categoryRepository.findAll();
        for (Category cat : categories) {
            if (cat.getIconUrl() != null && !cat.getIconUrl().isBlank()) {
                String newUrl = processCatalogImageUrl(cat.getIconUrl(), "categories");
                if (!newUrl.equals(cat.getIconUrl())) {
                    cat.setIconUrl(newUrl);
                    categoryRepository.save(cat);
                }
            }
            syncCategoryJsonToS3(cat);
            catCount++;
        }

        List<ServiceItem> items = serviceItemRepository.findAll();
        for (ServiceItem item : items) {
            if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                String newUrl = processCatalogImageUrl(item.getImageUrl(), "subcategories");
                if (!newUrl.equals(item.getImageUrl())) {
                    item.setImageUrl(newUrl);
                    serviceItemRepository.save(item);
                }
            }
            syncSubCategoryJsonToS3(item);
            subCount++;
        }

        List<Variant> variants = variantRepository.findAll();
        for (Variant var : variants) {
            if (var.getImageUrl() != null && !var.getImageUrl().isBlank()) {
                String newUrl = processCatalogImageUrl(var.getImageUrl(), "variants");
                if (!newUrl.equals(var.getImageUrl())) {
                    var.setImageUrl(newUrl);
                    variantRepository.save(var);
                }
            }
            syncVariantJsonToS3(var);
            varCount++;
        }

        String treeUrl = null;
        if (supabaseStorageService != null) {
            try {
                List<CategoryResponseDTO> tree = getActiveCategories();
                byte[] treeBytes = objectMapper.writeValueAsBytes(tree);
                treeUrl = supabaseStorageService.uploadCatalogJson("catalog/catalog-tree.json", treeBytes);
            } catch (Exception e) {
                log.warn("Notice: could not upload catalog-tree.json to S3: {}", e.getMessage());
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("categoriesSynced", catCount);
        result.put("subcategoriesSynced", subCount);
        result.put("variantsSynced", varCount);
        result.put("catalogTreeUrl", treeUrl);
        result.put("message", "Catalog successfully synchronized to S3 bucket");
        return result;
    }
}
