package com.salaryneeds.util;

import com.salaryneeds.exception.InvalidCatalogDataException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class CatalogNameNormalizer {

    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern UNSAFE_HTML_PATTERN = Pattern.compile(
            "<\\s*(script|iframe|style|svg|object|embed|link|form|img|body|html|meta|applet)[^>]*>|javascript:|onerror=|onload=",
            Pattern.CASE_INSENSITIVE
    );

    private CatalogNameNormalizer() {
    }

    public static String toLowerCaseNormalized(String name) {
        if (name == null) {
            return null;
        }
        String collapsed = MULTI_SPACE_PATTERN.matcher(name.trim()).replaceAll(" ");
        return collapsed.toLowerCase(Locale.ROOT);
    }

    public static void validateCatalogName(String name, String fieldName, int maxLength) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCatalogDataException(fieldName + " must not be blank");
        }
        String normalized = toLowerCaseNormalized(name);
        if (normalized.length() > maxLength) {
            throw new InvalidCatalogDataException(fieldName + " exceeds maximum allowed length of " + maxLength + " characters");
        }
        if (UNSAFE_HTML_PATTERN.matcher(name).find()) {
            throw new InvalidCatalogDataException(fieldName + " contains invalid or unsafe HTML content");
        }
    }

    public static void validateOptionalText(String text, String fieldName, int maxLength) {
        if (text != null) {
            if (text.length() > maxLength) {
                throw new InvalidCatalogDataException(fieldName + " exceeds maximum allowed length of " + maxLength + " characters");
            }
            if (UNSAFE_HTML_PATTERN.matcher(text).find()) {
                throw new InvalidCatalogDataException(fieldName + " contains invalid or unsafe HTML content");
            }
        }
    }
}
