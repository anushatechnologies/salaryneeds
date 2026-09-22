package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class AadharAlreadyExistsException extends ApiException {
    public AadharAlreadyExistsException(String message) {
        super("AADHAR_ALREADY_EXISTS", message, HttpStatus.CONFLICT);
    }

    public AadharAlreadyExistsException() {
        super("AADHAR_ALREADY_EXISTS", "Aadhaar number already exists", HttpStatus.CONFLICT);
    }
}
