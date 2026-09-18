package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class AccountNotActiveException extends ApiException {
    public AccountNotActiveException(String message) {
        super("ACCOUNT_NOT_ACTIVE", message, HttpStatus.FORBIDDEN);
    }

    public AccountNotActiveException() {
        super("ACCOUNT_NOT_ACTIVE", "Worker KYC is unverified or account is not active; cannot toggle On-Duty.", HttpStatus.FORBIDDEN);
    }
}
