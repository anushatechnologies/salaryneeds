package com.salaryneeds.service;

import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.SubCategory;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        if (categories.isEmpty()) {
            categories = seedDefaultCatalog();
        }

        List<Map<String, Object>> catList = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> cMap = new LinkedHashMap<>();
            cMap.put("id", cat.getId());
            cMap.put("name", cat.getName());
            cMap.put("code", cat.getCode());
            cMap.put("iconName", cat.getIconName());
            cMap.put("description", cat.getDescription());

            List<SubCategory> subs = subCategoryRepository.findByCategoryIdAndIsActiveTrue(cat.getId());
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

        Map<String, Object> response = new HashMap<>();
        response.put("categories", catList);
        return response;
    }

    private List<Category> seedDefaultCatalog() {
        Category c1 = Category.builder()
                .id("cat-elec-001")
                .name("Electrical Services")
                .code("cat-elec-001")
                .iconName("zap")
                .description("Wiring, MCB, switchboard, and appliance electrical repairs")
                .displayOrder(1)
                .build();
        categoryRepository.save(c1);

        SubCategory s1 = SubCategory.builder()
                .id("sub-cat-wiring-002")
                .categoryId(c1.getId())
                .name("House Wiring & Repairs")
                .code("sub-cat-wiring-002")
                .iconName("tool")
                .basePrice(BigDecimal.valueOf(300.00))
                .estimatedDuration("1 Hour")
                .build();
        subCategoryRepository.save(s1);

        Category c2 = Category.builder()
                .id("cat-hvac-002")
                .name("AC & HVAC Services")
                .code("cat-hvac-002")
                .iconName("wind")
                .description("Split & window AC deep servicing, gas refill, PCB repair")
                .displayOrder(2)
                .build();
        categoryRepository.save(c2);

        SubCategory s2 = SubCategory.builder()
                .id("sub-cat-ac-foam-001")
                .categoryId(c2.getId())
                .name("Split AC Foam Jet Wash")
                .code("sub-cat-ac-foam-001")
                .iconName("droplet")
                .basePrice(BigDecimal.valueOf(499.00))
                .estimatedDuration("1.5 Hours")
                .build();
        subCategoryRepository.save(s2);

        return List.of(c1, c2);
    }
}
