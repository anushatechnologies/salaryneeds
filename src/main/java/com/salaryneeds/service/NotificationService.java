package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerLocationResponseDTO;

import java.util.Map;

public interface NotificationService {

    void notifyWorkerOfOffer(String workerId, BookingOfferResponseDTO offer);

    void broadcastWorkerLocation(Long bookingId, WorkerLocationResponseDTO location);

    void notifyCustomerBookingEvent(String customerId, Long bookingId, com.salaryneeds.entity.enums.BookingStatus status, String title, String message, Map<String, Object> extraData);

    Map<String, Object> getNotifications(String workerId);

    Map<String, Object> getCustomerNotifications(String customerId);

    Map<String, Object> markAsRead(String notificationId, String workerId);
}
