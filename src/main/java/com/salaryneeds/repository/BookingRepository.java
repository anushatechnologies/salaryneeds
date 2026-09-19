package com.salaryneeds.repository;

import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByCustomerId(String customerId, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatusIn(String customerId, Collection<BookingStatus> statuses, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatus(String customerId, BookingStatus status, Pageable pageable);

    List<Booking> findByCustomerId(String customerId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByWorkerId(String workerId);

    List<Booking> findByWorkerIdAndStatus(String workerId, BookingStatus status);

    long countByServiceIdAndBookingDateAndSlotIdAndStatusNotIn(
            Long serviceId,
            LocalDate bookingDate,
            String slotId,
            Collection<BookingStatus> statuses
    );

    boolean existsByWorkerIdAndStatusIn(String workerId, Collection<BookingStatus> statuses);

    @Modifying
    @Query("UPDATE Booking b SET b.workerId = :workerId, b.status = :newStatus, b.acceptedAt = :now " +
           "WHERE b.id = :bookingId AND (b.workerId IS NULL OR b.workerId = '') AND b.status IN :assignableStatuses")
    int assignWorkerAtomically(
            @Param("bookingId") Long bookingId,
            @Param("workerId") String workerId,
            @Param("newStatus") BookingStatus newStatus,
            @Param("assignableStatuses") Collection<BookingStatus> assignableStatuses,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT b FROM Booking b WHERE b.workerId = :workerId AND (b.scheduledDate = :scheduledDate OR CAST(b.bookingDate AS string) = :scheduledDate) ORDER BY b.id ASC")
    List<Booking> findWorkerScheduleForDate(@Param("workerId") String workerId, @Param("scheduledDate") String scheduledDate);
}
