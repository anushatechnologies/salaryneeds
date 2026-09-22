package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.enums.BookingStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    List<SlotResponseDTO> getAvailableSlots(Long serviceId, LocalDate date);

    BookingResponseDTO createBooking(BookingCreateRequestDTO request, String customerIdHeader);

    PageResponseDTO<BookingResponseDTO> getBookings(String customerId, String statusFilter, Pageable pageable);

    BookingResponseDTO getBookingById(Long bookingId, String customerIdHeader);

    CancelBookingResponseDTO cancelBooking(Long bookingId, BookingCancelRequestDTO request, String customerIdHeader);

    BookingResponseDTO regenerateStartPin(Long bookingId, String customerIdHeader);

    BookingResponseDTO updateBookingStatus(Long bookingId, BookingStatus status, String workerId);

    BookingResponseDTO verifyPin(Long bookingId, String pin);

    WorkerActionResponseDTO startTravel(Long bookingId, String workerId);

    WorkerActionResponseDTO workerArrived(Long bookingId, String workerId);

    WorkerActionResponseDTO verifyStartPin(Long bookingId, String workerId, String pin);

    WorkerActionResponseDTO completeService(Long bookingId, String workerId);

    WorkerLocationResponseDTO updateWorkerLocation(Long bookingId, String workerId, WorkerLocationRequestDTO request);

    WorkerLocationResponseDTO getLatestWorkerLocation(Long bookingId);

    PaymentConfirmationResponseDTO confirmPayment(Long bookingId, String workerId, WorkerPaymentConfirmationRequestDTO request);
}
