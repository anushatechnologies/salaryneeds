package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerReviewRepository extends JpaRepository<WorkerReview, String> {
    List<WorkerReview> findByWorkerIdOrderByCreatedAtDesc(String workerId);
}
