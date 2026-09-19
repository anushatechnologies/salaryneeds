package com.salaryneeds.repository;

import com.salaryneeds.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    List<Job> findByCategoryContainingIgnoreCase(String category);
    List<Job> findByStatusIgnoreCase(String status);
}
