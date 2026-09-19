package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerLocationResponseDTO;

import java.util.Map;

public interface NotificationService {

    void notifyWorkerOfOffer(String workerId, BookingOfferResponseDTO offer);

    void broadcastWorkerLocation(Long bookingId, WorkerLocationResponseDTO location);

    Map<String, Object> getNotifications(String workerId);

    Map<String, Object> markAsRead(String notificationId, String workerId);
}
