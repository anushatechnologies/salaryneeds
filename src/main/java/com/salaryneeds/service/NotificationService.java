package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.dto.WorkerLocationResponseDTO;

public interface NotificationService {

    void notifyWorkerOfOffer(String workerId, BookingOfferResponseDTO offer);

    void broadcastWorkerLocation(Long bookingId, WorkerLocationResponseDTO location);
}

