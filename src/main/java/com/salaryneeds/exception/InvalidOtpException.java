package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends ApiException {
    public InvalidOtpException(String message) {
        super("INVALID_OTP", message, HttpStatus.BAD_REQUEST);
    }

    public InvalidOtpException() {
        super("INVALID_OTP", "Incorrect customer completion OTP. Please ask the customer for their 4-digit completion code.", HttpStatus.BAD_REQUEST);
    }
}
