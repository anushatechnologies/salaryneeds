package com.salaryneeds.service;

import com.salaryneeds.dto.catalog.*;
import com.salaryneeds.entity.CatalogServiceEntity;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.exception.*;
import com.salaryneeds.repository.CatalogServiceRepository;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.util.CatalogNameNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    // 2. CATEGORY (MIDDLE LEVEL)
    // ==========================================

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(UUID serviceId, CategoryRequestDTO request) {
        if (serviceId == null) {
            throw new InvalidCatalogDataException("Parent Service ID must not be null");
        }
        if (request == null) {
            throw new InvalidCatalogDataException("Request body must not be null");
        }

        CatalogServiceEntity parentService = catalogServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CatalogServiceNotFoundException("Parent Service not found with ID: " + serviceId));

        CatalogNameNormalizer.validateCatalogName(request.getName(), "Category name", 100);
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

        Category category = Category.builder()
                .service(parentService)
                .name(lowerCaseName)
                .description(request.getDescription())
                .iconUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(isActive)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created Category with ID: {} under Service: {}", saved.getId(), parentService.getName());
        return mapToCategoryResponseDTO(saved, false);
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
            CatalogNameNormalizer.validateCatalogName(request.getName(), "Category name", 100);
            String lowerCaseName = CatalogNameNormalizer.toLowerCaseNormalized(request.getName());

            UUID targetServiceId = targetService != null ? targetService.getId() : null;
            if (targetServiceId != null && categoryRepository.existsByServiceIdAndNameAndIdNot(targetServiceId, lowerCaseName, categoryId)) {
                throw new DuplicateCategoryException("A Category with the name '" + lowerCaseName +
                        "' already exists under parent Service '" + targetService.getName() + "'");
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

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
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
    // 3. SUB-CATEGORY (LEAF LEVEL)
    // ==========================================

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

        CatalogNameNormalizer.validateCatalogName(request.getName(), "Sub-Category name", 150);
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

        ServiceItem serviceItem = ServiceItem.builder()
                .category(parentCategory)
                .name(lowerCaseName)
                .description(request.getDescription())
                .basePrice(request.getBasePrice() != null ? request.getBasePrice() : BigDecimal.ZERO)
                .discountPrice(request.getDiscountPrice())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60)
                .inclusions(request.getInclusions())
                .exclusions(request.getExclusions())
                .imageUrl(request.getImageUrl())
                .isActive(isActive)
                .build();

        ServiceItem saved = serviceItemRepository.save(serviceItem);
        log.info("Created Sub-Category with ID: {} under parent Category: {}", saved.getId(), parentCategory.getName());
        return mapToSubCategoryResponseDTO(saved);
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
            CatalogNameNormalizer.validateCatalogName(request.getName(), "Sub-Category name", 150);
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

        if (request.getBasePrice() != null) {
            serviceItem.setBasePrice(request.getBasePrice());
        }

        if (request.getDiscountPrice() != null) {
            serviceItem.setDiscountPrice(request.getDiscountPrice());
        }

        if (request.getDurationMinutes() != null) {
            serviceItem.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getInclusions() != null) {
            serviceItem.setInclusions(request.getInclusions());
        }

        if (request.getExclusions() != null) {
            serviceItem.setExclusions(request.getExclusions());
        }

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
    // MAPPERS
    // ==========================================

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
                .imageUrl(category.getIconUrl())
                .displayOrder(category.getDisplayOrder())
                .status(category.getStatus())
                .subCategoriesCount(subCatsCount)
                .subCategories(subCats)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private SubCategoryResponseDTO mapToSubCategoryResponseDTO(ServiceItem item) {
        Category cat = item.getCategory();
        CatalogServiceEntity srv = (cat != null) ? cat.getService() : null;

        return SubCategoryResponseDTO.builder()
                .id(item.getId())
                .categoryId(cat != null ? cat.getId() : null)
                .categoryName(cat != null ? cat.getName() : null)
                .serviceId(srv != null ? srv.getId() : null)
                .serviceName(srv != null ? srv.getName() : null)
                .name(item.getName())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .discountPrice(item.getDiscountPrice())
                .durationMinutes(item.getDurationMinutes())
                .inclusions(item.getInclusions())
                .exclusions(item.getExclusions())
                .imageUrl(item.getImageUrl())
                .ratingAvg(item.getRatingAvg())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
