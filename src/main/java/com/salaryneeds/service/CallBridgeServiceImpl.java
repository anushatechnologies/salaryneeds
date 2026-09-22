package com.salaryneeds.service;

import com.salaryneeds.dto.CallResponseDTO;
import com.salaryneeds.dto.InitiateCallRequestDTO;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.exception.BookingNotFoundException;
import com.salaryneeds.exception.InvalidBookingStateException;
import com.salaryneeds.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallBridgeServiceImpl implements CallBridgeService {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public CallResponseDTO initiateMaskedCall(Long bookingId, String customerIdHeader, String workerIdHeader, InitiateCallRequestDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        String callerType = "CUSTOMER";
        boolean isCustomer = customerIdHeader != null && !customerIdHeader.isBlank() && customerIdHeader.trim().equals(booking.getCustomerId());
        boolean isWorker = workerIdHeader != null && !workerIdHeader.isBlank() && workerIdHeader.trim().equals(booking.getWorkerId());

        if (isWorker) {
            callerType = "WORKER";
        } else if (!isCustomer && customerIdHeader != null && !customerIdHeader.isBlank()) {
            throw new InvalidBookingStateException("Unauthorized caller: ID does not match booking customer or worker");
        }

        // Validate booking is in an active contactable state
        BookingStatus status = booking.getStatus();
        boolean isActive = status == BookingStatus.ACCEPTED
                || status == BookingStatus.ASSIGNED
                || status == BookingStatus.EN_ROUTE
                || status == BookingStatus.WORKER_ON_THE_WAY
                || status == BookingStatus.ARRIVED
                || status == BookingStatus.IN_PROGRESS;

        if (!isActive) {
            throw new InvalidBookingStateException("Direct calling is only available for active bookings (ACCEPTED to IN_PROGRESS). Current status: " + status);
        }

        if (booking.getWorkerId() == null || booking.getWorkerId().isBlank()) {
            if (workerIdHeader != null && !workerIdHeader.isBlank()) {
                booking.setWorkerId(workerIdHeader.trim());
            } else {
                booking.setWorkerId("W-104");
            }
            bookingRepository.save(booking);
        }

        // In production: trigger Twilio / Exotel Click-to-Call Voice API
        String sessionId = "CALL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String maskedNumber = "+91 80 47" + (1000 + (int)(Math.random() * 9000));

        log.info("Initiated secure masked call session {} for booking #{} by {}", sessionId, bookingId, callerType);

        return CallResponseDTO.builder()
                .callSessionId(sessionId)
                .bookingId(bookingId)
                .callerType(callerType)
                .status("CONNECTING")
                .maskedDisplayNumber(maskedNumber)
                .message("Connecting to " + ("CUSTOMER".equals(callerType) ? "Worker" : "Customer") + " via secure masked bridge. Your phone will ring in 5 seconds.")
                .estimatedWaitSeconds(5)
                .initiatedAt(LocalDateTime.now())
                .build();
    }
}
