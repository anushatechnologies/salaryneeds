package com.salaryneeds.exception;

public class CatalogServiceNotFoundException extends RuntimeException {
    public CatalogServiceNotFoundException(String message) {
        super(message);
    }
}
