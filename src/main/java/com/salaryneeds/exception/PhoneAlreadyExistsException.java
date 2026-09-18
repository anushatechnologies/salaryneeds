package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class PhoneAlreadyExistsException extends ApiException {
    public PhoneAlreadyExistsException(String message) {
        super("PHONE_ALREADY_EXISTS", message, HttpStatus.CONFLICT);
    }

    public PhoneAlreadyExistsException() {
        super("PHONE_ALREADY_EXISTS", "Worker mobile number is already registered.", HttpStatus.CONFLICT);
    }
}
