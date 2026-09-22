package com.salaryneeds.repository;

import com.salaryneeds.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart_Id(Long cartId);

    Optional<CartItem> findByIdAndCart_Id(Long id, Long cartId);

    Optional<CartItem> findByCart_IdAndServiceId(Long cartId, Long serviceId);

    void deleteByCart_Id(Long cartId);
}
