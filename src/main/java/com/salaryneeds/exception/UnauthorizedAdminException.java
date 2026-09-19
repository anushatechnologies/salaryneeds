package com.salaryneeds.exception;

public class UnauthorizedAdminException extends RuntimeException {
    public UnauthorizedAdminException(String message) {
        super(message);
    }
}
