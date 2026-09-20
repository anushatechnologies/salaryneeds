package com.salaryneeds.repository;

import com.salaryneeds.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByIsActiveOrderByNameAsc(Boolean isActive);

    List<Category> findAllByOrderByNameAsc();

    default List<Category> findByIsActiveOrderByDisplayOrderAscNameAsc(Boolean isActive) {
        return findByIsActiveOrderByNameAsc(isActive);
    }

    default List<Category> findAllByOrderByDisplayOrderAscNameAsc() {
        return findAllByOrderByNameAsc();
    }

    Optional<Category> findByIdAndIsActiveTrue(UUID id);

    Optional<Category> findByName(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    @Query("SELECT c FROM Category c WHERE " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.name ASC")
    List<Category> searchCategories(@Param("search") String search, @Param("isActive") Boolean isActive);

    default List<Category> searchCategories(UUID serviceId, String search, Boolean isActive) {
        return searchCategories(search, isActive);
    }

    default List<Category> findByIsActiveTrueOrderByDisplayOrderAsc() {
        return findByIsActiveOrderByNameAsc(true);
    }
}
