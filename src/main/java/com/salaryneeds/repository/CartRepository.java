package com.salaryneeds.repository;

import com.salaryneeds.entity.Cart;
import com.salaryneeds.entity.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomerIdAndStatus(String customerId, CartStatus status);

    Optional<Cart> findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, CartStatus status);

    boolean existsByCustomerIdAndStatus(String customerId, CartStatus status);
}
