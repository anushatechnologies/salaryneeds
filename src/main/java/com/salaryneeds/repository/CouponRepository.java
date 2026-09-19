package com.salaryneeds.repository;

import com.salaryneeds.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    Page<Coupon> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<Coupon> findByIsActiveAndCodeContainingIgnoreCase(Boolean isActive, String code, Pageable pageable);
}
