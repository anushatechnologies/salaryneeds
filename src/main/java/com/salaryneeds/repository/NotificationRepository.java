package com.salaryneeds.repository;

import com.salaryneeds.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByWorkerIdOrderByCreatedAtDesc(String workerId);
    List<Notification> findByWorkerIdAndIsReadFalse(String workerId);
}
