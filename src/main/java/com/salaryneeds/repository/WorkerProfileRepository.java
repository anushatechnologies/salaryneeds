package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID> {
    Optional<WorkerProfile> findByPhone(String phone);
    boolean existsByPhone(String phone);
}
