package com.salaryneeds.repository;

import com.salaryneeds.entity.CatalogServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogServiceRepository extends JpaRepository<CatalogServiceEntity, UUID> {

    List<CatalogServiceEntity> findByIsActiveOrderByDisplayOrderAscNameAsc(Boolean isActive);

    List<CatalogServiceEntity> findAllByOrderByDisplayOrderAscNameAsc();

    Optional<CatalogServiceEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    @Query("SELECT s FROM CatalogServiceEntity s WHERE " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY s.displayOrder ASC, s.name ASC")
    List<CatalogServiceEntity> searchServices(@Param("search") String search, @Param("isActive") Boolean isActive);
}
