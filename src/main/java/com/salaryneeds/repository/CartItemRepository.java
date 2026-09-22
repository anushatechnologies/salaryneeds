package com.salaryneeds.repository;

import com.salaryneeds.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndServiceId(Long cartId, Long serviceId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    List<CartItem> findByCartId(Long cartId);

    void deleteByCartId(Long cartId);
}
