package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, String> {
    Optional<WorkerProfile> findByPhone(String phone);
    boolean existsByPhone(String phone);
    List<WorkerProfile> findByDutyOnlineTrue();
    List<WorkerProfile> findByDutyOnlineTrueAndCategoryId(String categoryId);
}
