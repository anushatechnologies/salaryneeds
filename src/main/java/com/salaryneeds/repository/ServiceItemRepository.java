package com.salaryneeds.repository;

import com.salaryneeds.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByCategoryIdAndIsActiveTrue(UUID categoryId);

    List<ServiceItem> findByIsActiveTrue();

    Optional<ServiceItem> findByIdAndIsActiveTrue(Long id);
}
