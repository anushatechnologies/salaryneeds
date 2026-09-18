package com.salaryneeds.service;

import com.salaryneeds.entity.Notification;
import com.salaryneeds.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getNotifications(String workerId) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        List<Notification> list = notificationRepository.findByWorkerIdOrderByCreatedAtDesc(workerId);
        if (list.isEmpty()) {
            Notification n1 = Notification.builder()
                    .id("notif-101")
                    .workerId(workerId)
                    .title("Job Dispatch Alert")
                    .message("New House Wiring job available near Indiranagar")
                    .type("JOB_ALERT")
                    .dataJson("{\"booking_id\":\"SNB-2505187\"}")
                    .isRead(false)
                    .build();
            notificationRepository.save(n1);
            list = List.of(n1);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", list);
        return response;
    }

    @Transactional
    public Map<String, Object> markAsRead(String notificationId, String workerId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification marked as read");
        return response;
    }
}
