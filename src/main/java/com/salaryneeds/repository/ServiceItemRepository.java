package com.salaryneeds.repository;

import com.salaryneeds.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByCategoryIdAndIsActiveTrue(UUID categoryId);

    List<ServiceItem> findByCategoryId(UUID categoryId);

    List<ServiceItem> findByIsActiveTrue();

    Optional<ServiceItem> findByIdAndIsActiveTrue(Long id);

    boolean existsByCategoryIdAndName(UUID categoryId, String name);

    boolean existsByCategoryIdAndNameIgnoreCase(UUID categoryId, String name);

    boolean existsByCategoryIdAndNameAndIdNot(UUID categoryId, String name, Long id);

    int countByCategoryId(UUID categoryId);

    int countByCategoryIdAndIsActiveTrue(UUID categoryId);

    @Query("SELECT s FROM ServiceItem s WHERE s.category.id = :categoryId AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) " +
           "ORDER BY s.name ASC")
    List<ServiceItem> findByCategoryIdAndStatus(@Param("categoryId") UUID categoryId, @Param("isActive") Boolean isActive);
}
