package com.salaryneeds.repository;

import com.salaryneeds.entity.BookingExtraPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingExtraPartRepository extends JpaRepository<BookingExtraPart, String> {
    List<BookingExtraPart> findByBookingId(String bookingId);
}
