package com.salaryneeds.repository;

import com.salaryneeds.entity.Cart;
import com.salaryneeds.entity.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomerIdAndStatus(UUID customerId, CartStatus status);

    Optional<Cart> findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(UUID customerId, CartStatus status);

    List<Cart> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    boolean existsByCustomerIdAndStatus(UUID customerId, CartStatus status);
}
