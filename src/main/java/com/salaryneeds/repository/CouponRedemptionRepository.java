package com.salaryneeds.repository;

import com.salaryneeds.entity.CouponRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, UUID> {

    Optional<CouponRedemption> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    long countByCouponId(Long couponId);

    long countByCouponIdAndCustomerId(Long couponId, String customerId);

    List<CouponRedemption> findByCouponId(Long couponId);

    Page<CouponRedemption> findByCouponId(Long couponId, Pageable pageable);

    void deleteByBookingId(Long bookingId);
}
