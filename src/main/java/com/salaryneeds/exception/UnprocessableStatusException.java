package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableStatusException extends ApiException {
    public UnprocessableStatusException(String message) {
        super("UNPROCESSABLE_STATUS", message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
