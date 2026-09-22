package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class PanAlreadyExistsException extends ApiException {
    public PanAlreadyExistsException(String message) {
        super("PAN_ALREADY_EXISTS", message, HttpStatus.CONFLICT);
    }

    public PanAlreadyExistsException() {
        super("PAN_ALREADY_EXISTS", "PAN number already exists", HttpStatus.CONFLICT);
    }
}
