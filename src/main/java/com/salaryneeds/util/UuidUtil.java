package com.salaryneeds.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UuidUtil {

    private UuidUtil() {}

    public static UUID parseUuid(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        String cleaned = str.trim();
        if (cleaned.startsWith("w-") || cleaned.startsWith("c-") || cleaned.startsWith("b-")) {
            cleaned = cleaned.substring(2);
        }
        try {
            return UUID.fromString(cleaned);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(str.trim().getBytes(StandardCharsets.UTF_8));
        }
    }
}
