package com.salaryneeds.service;

import com.salaryneeds.dto.BookingOfferResponseDTO;
import com.salaryneeds.entity.Booking;

import java.util.List;

public interface WorkerMatchingService {

    /**
     * Finds eligible workers for the given booking and generates booking offers.
     *
     * @param booking The booking to match workers for
     * @return List of generated booking offers sorted by distance
     */
    List<BookingOfferResponseDTO> matchAndCreateOffers(Booking booking);
}
