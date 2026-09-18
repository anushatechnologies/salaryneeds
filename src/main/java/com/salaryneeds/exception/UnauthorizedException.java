package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Expired, invalid, or missing Bearer token.", HttpStatus.UNAUTHORIZED);
    }
}
