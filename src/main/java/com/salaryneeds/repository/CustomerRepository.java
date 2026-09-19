package com.salaryneeds.repository;

import com.salaryneeds.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ── Admin-facing queries ──────────────────────────────────────────────

    Page<Customer> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Customer> findAllByAccountStatusOrderByCreatedAtDesc(String accountStatus, Pageable pageable);

    long countByAccountStatus(String accountStatus);

}
