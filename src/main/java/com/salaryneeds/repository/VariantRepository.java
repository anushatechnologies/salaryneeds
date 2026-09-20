package com.salaryneeds.repository;

import com.salaryneeds.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VariantRepository extends JpaRepository<Variant, Long> {

    List<Variant> findBySubcategoryId(Long subcategoryId);

    List<Variant> findBySubcategoryIdAndIsActiveTrue(Long subcategoryId);

    Optional<Variant> findByIdAndIsActiveTrue(Long id);

    boolean existsBySubcategoryIdAndName(Long subcategoryId, String name);

    boolean existsBySubcategoryIdAndNameIgnoreCase(Long subcategoryId, String name);

    boolean existsBySubcategoryIdAndNameAndIdNot(Long subcategoryId, String name, Long id);

    int countBySubcategoryId(Long subcategoryId);

    int countBySubcategoryIdAndIsActiveTrue(Long subcategoryId);

    @Query("SELECT v FROM Variant v WHERE v.subcategory.id = :subcategoryId AND " +
           "(:isActive IS NULL OR v.isActive = :isActive) " +
           "ORDER BY v.name ASC")
    List<Variant> findBySubcategoryIdAndStatus(@Param("subcategoryId") Long subcategoryId, @Param("isActive") Boolean isActive);
}
