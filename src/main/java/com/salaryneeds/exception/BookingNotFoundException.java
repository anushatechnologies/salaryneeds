package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class BookingNotFoundException extends ApiException {
    public BookingNotFoundException(String bookingId) {
        super("BOOKING_NOT_FOUND", "Booking with ID '" + bookingId + "' does not exist.", HttpStatus.NOT_FOUND);
    }
}
