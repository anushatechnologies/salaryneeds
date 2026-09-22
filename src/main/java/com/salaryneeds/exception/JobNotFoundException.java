package com.salaryneeds.exception;

public class JobNotFoundException extends BookingNotFoundException {
    public JobNotFoundException(String message) {
        super(message);
    }
}
