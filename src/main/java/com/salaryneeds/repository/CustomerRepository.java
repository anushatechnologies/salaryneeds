package com.salaryneeds.repository;

import com.salaryneeds.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);

    Optional<Customer> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ── Admin-facing queries ──────────────────────────────────────────────

    Page<Customer> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Customer> findAllByAccountStatusOrderByCreatedAtDesc(String accountStatus, Pageable pageable);

    long countByAccountStatus(String accountStatus);

    // ── Normalized login queries ──────────────────────────────────────────

    @Query("SELECT c FROM Customer c WHERE LOWER(TRIM(c.email)) = LOWER(TRIM(:identifier)) OR TRIM(c.phone) = TRIM(:identifier)")
    Optional<Customer> findByEmailOrPhoneNormalized(@Param("identifier") String identifier);

    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE LOWER(TRIM(c.email)) = LOWER(TRIM(:email))")
    boolean existsByNormalizedEmail(@Param("email") String email);

    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE TRIM(c.phone) = TRIM(:phone)")
    boolean existsByNormalizedPhone(@Param("phone") String phone);
}
