package com.salaryneeds.repository;

import com.salaryneeds.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, String> {
    List<SupportTicket> findByWorkerIdOrderByCreatedAtDesc(String workerId);
}
