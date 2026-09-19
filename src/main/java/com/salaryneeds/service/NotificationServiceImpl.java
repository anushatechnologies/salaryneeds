package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

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
    public void broadcastWorkerLocation(Long bookingId, com.salaryneeds.dto.WorkerLocationResponseDTO location) {
        String destination = "/topic/bookings/" + bookingId + "/worker-location";
        try {
            messagingTemplate.convertAndSend(destination, location);
            log.debug("Broadcast worker location for booking #{} to {}", bookingId, destination);
        } catch (Exception e) {
            log.warn("Could not broadcast worker location to {}: {}", destination, e.getMessage());
        }
    }
}
