package com.salaryneeds.service;

import com.salaryneeds.dto.CategoryDTO;
import com.salaryneeds.dto.ServiceItemDTO;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.exception.ServiceNotFoundException;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceItemDTO> getServicesByCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ServiceNotFoundException("Category not found with id: " + categoryId));

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

    private CategoryDTO mapCategoryToDTO(Category category) {
        List<ServiceItem> services = serviceItemRepository.findByCategoryIdAndIsActiveTrue(category.getId());
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .servicesCount(services.size())
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
                .discountPrice(item.getDiscountPrice())
                .durationMinutes(item.getDurationMinutes())
                .inclusions(item.getInclusions())
                .exclusions(item.getExclusions())
                .imageUrl(item.getImageUrl())
                .ratingAvg(item.getRatingAvg())
                .isActive(item.getIsActive())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
