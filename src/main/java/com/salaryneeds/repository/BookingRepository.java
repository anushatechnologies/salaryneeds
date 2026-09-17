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
}
