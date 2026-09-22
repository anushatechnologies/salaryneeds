package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerLocationResponseDTO;
import com.salaryneeds.entity.Notification;
import com.salaryneeds.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    @Override
    public void notifyWorkerOfOffer(String workerId, BookingOfferResponseDTO offer) {
        String destination = "/topic/worker/" + workerId + "/offers";
        try {
            messagingTemplate.convertAndSend(destination, offer);
            log.info("Dispatched real-time offer #{} to worker {} via WebSocket {}", offer.getId(), workerId, destination);
        } catch (Exception e) {
            log.warn("Could not dispatch WebSocket notification to {}: {}", destination, e.getMessage());
        }
    }

    @Override
    public void broadcastWorkerLocation(Long bookingId, WorkerLocationResponseDTO location) {
        String destination = "/topic/bookings/" + bookingId + "/worker-location";
        try {
            messagingTemplate.convertAndSend(destination, location);
            log.debug("Broadcast worker location for booking #{} to {}", bookingId, destination);
        } catch (Exception e) {
            log.warn("Could not broadcast worker location to {}: {}", destination, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void notifyCustomerBookingEvent(String customerId, Long bookingId, com.salaryneeds.entity.enums.BookingStatus status, String title, String message, Map<String, Object> extraData) {
        if (customerId == null || customerId.isBlank()) return;

        // 1. Save to database
        String notifId = "notif-c-" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", notifId);
        payload.put("bookingId", bookingId);
        payload.put("status", status != null ? status.name() : null);
        payload.put("title", title);
        payload.put("message", message);
        payload.put("timestamp", LocalDateTime.now());
        if (extraData != null) {
            payload.putAll(extraData);
        }

        try {
            Notification notification = Notification.builder()
                    .id(notifId)
                    .customerId(customerId)
                    .recipientType("CUSTOMER")
                    .title(title)
                    .message(message)
                    .type("BOOKING_STATUS_" + (status != null ? status.name() : "UPDATE"))
                    .dataJson(payload.toString())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.warn("Failed to persist customer notification: {}", e.getMessage());
        }

        // 2. Broadcast to customer WebSocket channel: /topic/customer/{customerId}/bookings
        String customerChannel = "/topic/customer/" + customerId + "/bookings";
        try {
            messagingTemplate.convertAndSend(customerChannel, payload);
            log.info("Dispatched live notification to customer {} via {}: {}", customerId, customerChannel, title);
        } catch (Exception e) {
            log.warn("Could not dispatch customer WebSocket notification to {}: {}", customerChannel, e.getMessage());
        }

        // 3. Also broadcast to booking status channel: /topic/bookings/{bookingId}/status
        if (bookingId != null) {
            String bookingChannel = "/topic/bookings/" + bookingId + "/status";
            try {
                messagingTemplate.convertAndSend(bookingChannel, payload);
            } catch (Exception e) {
                log.warn("Could not dispatch status update to {}: {}", bookingChannel, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerNotifications(String customerId) {
        if (customerId == null || customerId.isBlank()) customerId = "c-default";

        List<Notification> list = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", list);
        return response;
    }

    @Override
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

    @Override
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
