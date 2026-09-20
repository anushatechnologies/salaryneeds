package com.salaryneeds.service;

import com.salaryneeds.dto.CategoryDTO;
import com.salaryneeds.dto.ServiceItemDTO;
import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.exception.CategoryNotFoundException;
import com.salaryneeds.exception.ServiceNotFoundException;
import com.salaryneeds.exception.SubCategoryNotFoundException;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import com.salaryneeds.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final VariantRepository variantRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findByIsActiveOrderByDisplayOrderAscNameAsc(true)
                .stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
        return mapCategoryToDTO(category);
    }

    @Override
    public List<ServiceItemDTO> getServicesByCategory(UUID categoryId) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        return serviceItemRepository.findByCategoryIdAndIsActiveTrue(category.getId())
                .stream()
                .map(this::mapServiceItemToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceItemDTO> getAllServices() {
        return serviceItemRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapServiceItemToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceItemDTO getServiceById(Long serviceId) {
        ServiceItem serviceItem = serviceItemRepository.findByIdAndIsActiveTrue(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found with id: " + serviceId));
        return mapServiceItemToDTO(serviceItem);
    }

    // ==========================================
    // PUBLIC ACTIVE CATALOG METHODS
    // ==========================================

    @Override
    public List<CategoryResponseDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveOrderByDisplayOrderAscNameAsc(true)
                .stream()
                .map(this::mapToCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubCategoryResponseDTO> getActiveSubcategoriesByCategory(UUID categoryId) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Active category not found with ID: " + categoryId));

        return serviceItemRepository.findByCategoryIdAndIsActiveTrue(category.getId())
                .stream()
                .map(this::mapToSubCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubCategoryResponseDTO getActiveSubcategoryById(Long subcategoryId) {
        ServiceItem item = serviceItemRepository.findByIdAndIsActiveTrue(subcategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Active subcategory not found with ID: " + subcategoryId));

        // Check if parent category is active
        if (item.getCategory() != null && Boolean.FALSE.equals(item.getCategory().getIsActive())) {
            throw new SubCategoryNotFoundException("Parent category is inactive for subcategory ID: " + subcategoryId);
        }

        return mapToSubCategoryResponseDTO(item);
    }

    @Override
    public List<VariantResponseDTO> getActiveVariantsBySubcategory(Long subcategoryId) {
        ServiceItem subcategory = serviceItemRepository.findByIdAndIsActiveTrue(subcategoryId)
                .orElseThrow(() -> new SubCategoryNotFoundException("Active subcategory not found with ID: " + subcategoryId));

        if (subcategory.getCategory() != null && Boolean.FALSE.equals(subcategory.getCategory().getIsActive())) {
            throw new SubCategoryNotFoundException("Parent category is inactive for subcategory ID: " + subcategoryId);
        }

        return variantRepository.findBySubcategoryIdAndIsActiveTrue(subcategoryId)
                .stream()
                .map(this::mapToVariantResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // MAPPERS
    // ==========================================

    private CategoryDTO mapCategoryToDTO(Category category) {
        int servicesCount = serviceItemRepository.countByCategoryIdAndIsActiveTrue(category.getId());
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .amount(category.getAmount() != null ? category.getAmount() : BigDecimal.ZERO)
                .discount(category.getDiscount() != null ? category.getDiscount() : BigDecimal.ZERO)
                .finalAmount(category.getFinalAmount() != null ? category.getFinalAmount() : BigDecimal.ZERO)
                .iconUrl(category.getIconUrl())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .servicesCount(servicesCount)
                .createdAt(category.getCreatedAt())
                .build();
    }

    private ServiceItemDTO mapServiceItemToDTO(ServiceItem item) {
        return ServiceItemDTO.builder()
                .id(item.getId())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .name(item.getName())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .discountPrice(item.getFinalAmount() != null ? item.getFinalAmount() : item.getDiscountPrice())
                .imageUrl(item.getImageUrl())
                .isActive(item.getIsActive())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private CategoryResponseDTO mapToCategoryResponseDTO(Category category) {
        int subCatsCount = serviceItemRepository.countByCategoryIdAndIsActiveTrue(category.getId());
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
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private SubCategoryResponseDTO mapToSubCategoryResponseDTO(ServiceItem item) {
        Category cat = item.getCategory();
        int variantsCount = variantRepository.countBySubcategoryIdAndIsActiveTrue(item.getId());

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
}
