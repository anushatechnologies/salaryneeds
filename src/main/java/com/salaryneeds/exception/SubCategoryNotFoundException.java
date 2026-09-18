package com.salaryneeds.exception;

public class SubCategoryNotFoundException extends RuntimeException {
    public SubCategoryNotFoundException(String message) {
        super(message);
    }
}
