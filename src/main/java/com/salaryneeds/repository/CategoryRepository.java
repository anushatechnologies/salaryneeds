package com.salaryneeds.repository;

import com.salaryneeds.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<Category> findByNameIgnoreCase(String name);
}
