package com.salaryneeds.exception;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {

    private Map<String, String> errors;

    public ValidationErrorResponse(String message, int status, LocalDateTime timestamp, Map<String, String> errors) {
        super(message, status, timestamp);
        this.errors = errors;
    }
}
