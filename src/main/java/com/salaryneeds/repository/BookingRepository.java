package com.salaryneeds.repository;

import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByCustomerId(String customerId, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatusIn(String customerId, Collection<BookingStatus> statuses, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatus(String customerId, BookingStatus status, Pageable pageable);

    List<Booking> findByCustomerId(String customerId);

    long countByServiceIdAndBookingDateAndSlotIdAndStatusNotIn(
            Long serviceId,
            LocalDate bookingDate,
            String slotId,
            Collection<BookingStatus> statuses
    );

    boolean existsByWorkerIdAndStatusIn(String workerId, Collection<BookingStatus> statuses);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Booking b SET b.workerId = :workerId, b.status = :newStatus, b.acceptedAt = :now " +
           "WHERE b.id = :bookingId AND (b.workerId IS NULL OR b.workerId = '') AND b.status IN :assignableStatuses")
    int assignWorkerAtomically(
            @org.springframework.data.repository.query.Param("bookingId") Long bookingId,
            @org.springframework.data.repository.query.Param("workerId") String workerId,
            @org.springframework.data.repository.query.Param("newStatus") BookingStatus newStatus,
            @org.springframework.data.repository.query.Param("assignableStatuses") Collection<BookingStatus> assignableStatuses,
            @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now
    );
}
