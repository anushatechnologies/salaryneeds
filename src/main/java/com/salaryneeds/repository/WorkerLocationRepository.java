package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerLocationRepository extends JpaRepository<WorkerLocation, Long> {

    Optional<WorkerLocation> findFirstByBookingIdOrderByTimestampDesc(Long bookingId);

    List<WorkerLocation> findByBookingIdOrderByTimestampAsc(Long bookingId);
}
