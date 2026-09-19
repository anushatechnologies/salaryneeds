package com.salaryneeds.repository;

import com.salaryneeds.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Filter query supporting optional action keyword, description keyword, and date range.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR UPPER(a.action) LIKE UPPER(CONCAT('%', :action, '%'))) AND " +
           "(:description IS NULL OR UPPER(a.description) LIKE UPPER(CONCAT('%', :description, '%'))) AND " +
           "(:from IS NULL OR a.createdAt >= :from) AND " +
           "(:to   IS NULL OR a.createdAt <= :to) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findWithFilters(
            @Param("action")      String action,
            @Param("description") String description,
            @Param("from")        LocalDateTime from,
            @Param("to")          LocalDateTime to,
            Pageable pageable
    );
}
