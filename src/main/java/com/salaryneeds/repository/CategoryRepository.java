package com.salaryneeds.repository;

import com.salaryneeds.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByIsActiveOrderByDisplayOrderAscNameAsc(Boolean isActive);

    List<Category> findAllByOrderByDisplayOrderAscNameAsc();

    List<Category> findByServiceId(UUID serviceId);

    List<Category> findByServiceIdAndIsActiveTrue(UUID serviceId);

    int countByServiceId(UUID serviceId);

    Optional<Category> findByIdAndIsActiveTrue(UUID id);

    Optional<Category> findByName(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsByServiceIdAndName(UUID serviceId, String name);

    boolean existsByServiceIdAndNameAndIdNot(UUID serviceId, String name, UUID id);

    @Query("SELECT c FROM Category c WHERE " +
           "(:serviceId IS NULL OR c.service.id = :serviceId) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> searchCategories(@Param("serviceId") UUID serviceId, @Param("search") String search, @Param("isActive") Boolean isActive);
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
}
