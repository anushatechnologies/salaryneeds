package com.salaryneeds.repository;

import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByWorkerId(String workerId);
    List<Booking> findByWorkerIdAndStatus(String workerId, BookingStatus status);
    List<Booking> findByWorkerIdAndScheduledDate(String workerId, String scheduledDate);

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND (b.workerId IS NULL OR b.workerId = '')")
    List<Booking> findPendingUnassignedLeads();

    @Query("SELECT b FROM Booking b WHERE b.workerId = :workerId AND b.scheduledDate = :scheduledDate ORDER BY b.slotStart ASC")
    List<Booking> findWorkerScheduleForDate(@Param("workerId") String workerId, @Param("scheduledDate") String scheduledDate);
}
