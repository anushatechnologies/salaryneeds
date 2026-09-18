package com.salaryneeds.repository;

import com.salaryneeds.entity.LocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationLogRepository extends JpaRepository<LocationLog, String> {
    List<LocationLog> findTop20ByWorkerIdOrderByCreatedAtDesc(String workerId);
}
