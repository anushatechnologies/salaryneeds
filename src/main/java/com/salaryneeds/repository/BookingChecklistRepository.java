package com.salaryneeds.repository;

import com.salaryneeds.entity.BookingChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingChecklistRepository extends JpaRepository<BookingChecklist, String> {
    List<BookingChecklist> findByBookingId(String bookingId);
}
