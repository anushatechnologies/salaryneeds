package com.salaryneeds.dto.catalog;

import com.salaryneeds.exception.InvalidCatalogDataException;

public enum CatalogStatus {
    ACTIVE,
    INACTIVE;

    public static CatalogStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return CatalogStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCatalogDataException("Invalid status value: '" + value + "'. Allowed values: ACTIVE, INACTIVE");
        }
    }

    public static boolean toBoolean(CatalogStatus status) {
        return status == null || status == ACTIVE;
    }

    public static CatalogStatus fromBoolean(Boolean isActive) {
        return (isActive != null && isActive) ? ACTIVE : INACTIVE;
    }
}
