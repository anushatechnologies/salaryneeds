package com.salaryneeds.repository;

import com.salaryneeds.entity.Review;
import com.salaryneeds.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Review> findAllByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);

    long countByStatus(ReviewStatus status);

    Page<Review> findByWorkerId(UUID workerId, Pageable pageable);
}
