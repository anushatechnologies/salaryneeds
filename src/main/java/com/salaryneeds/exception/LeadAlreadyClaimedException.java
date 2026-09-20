package com.salaryneeds.exception;

import org.springframework.http.HttpStatus;

public class LeadAlreadyClaimedException extends ApiException {
    public LeadAlreadyClaimedException(String message) {
        super("LEAD_ALREADY_CLAIMED", message, HttpStatus.CONFLICT);
    }

    public LeadAlreadyClaimedException() {
        super("LEAD_ALREADY_CLAIMED", "Another technician has already accepted this dispatch lead.", HttpStatus.CONFLICT);
    }
}
