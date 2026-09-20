package com.salaryneeds.exception;

public class DuplicateSubCategoryException extends RuntimeException {
    public DuplicateSubCategoryException(String message) {
        super(message);
    }
}
