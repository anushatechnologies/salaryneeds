package com.salaryneeds.service;

import com.salaryneeds.dto.CategoryDTO;
import com.salaryneeds.dto.ServiceItemDTO;
import com.salaryneeds.dto.catalog.CategoryResponseDTO;
import com.salaryneeds.dto.catalog.SubCategoryResponseDTO;
import com.salaryneeds.dto.catalog.VariantResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CatalogService {

    // Existing methods for backward compatibility
    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(UUID categoryId);

    List<ServiceItemDTO> getServicesByCategory(UUID categoryId);

    List<ServiceItemDTO> getAllServices();

            List<SubCategory> subs = subCategoryRepository.findByCategoryIdAndIsActiveTrue(cat.getId());
            if (subs.isEmpty()) {
                SubCategory defaultSub = SubCategory.builder()
                        .id("sub-" + cat.getId() + "-default")
                        .categoryId(cat.getId())
                        .name(cat.getName() + " Standard Service")
                        .code("sub-" + cat.getCode() + "-def")
                        .iconName(cat.getIconName())
                        .basePrice(BigDecimal.valueOf(399.00))
                        .estimatedDuration("1 Hour")
                        .isActive(true)
                        .build();
                subCategoryRepository.save(defaultSub);
                subs = List.of(defaultSub);
            }
            List<Map<String, Object>> subList = new ArrayList<>();
            for (SubCategory s : subs) {
                Map<String, Object> sMap = new LinkedHashMap<>();
                sMap.put("id", s.getId());
                sMap.put("name", s.getName());
                sMap.put("code", s.getCode());
                sMap.put("basePrice", s.getBasePrice());
                sMap.put("estimatedDuration", s.getEstimatedDuration());
                subList.add(sMap);
            }
            cMap.put("subCategories", subList);
            catList.add(cMap);
        }

    Map<String, Object> getCategories();
}
