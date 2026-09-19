package com.salaryneeds.service;

import com.salaryneeds.dto.CategoryDTO;
import com.salaryneeds.dto.ServiceItemDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CatalogService {

    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(UUID categoryId);

    List<ServiceItemDTO> getServicesByCategory(UUID categoryId);

    List<ServiceItemDTO> getAllServices();

    ServiceItemDTO getServiceById(Long serviceId);

    Map<String, Object> getCategories();
}
