package com.salaryneeds.repository;

import com.salaryneeds.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, String> {
    List<JobApplication> findByWorkerId(String workerId);
    Optional<JobApplication> findByJobIdAndWorkerId(String jobId, String workerId);
    boolean existsByJobIdAndWorkerId(String jobId, String workerId);
}
