package com.pos.util;

import java.util.regex.Pattern;

/** Central validation for user-visible plain-text fields. */
public final class InputSafety {
    private static final Pattern MARKUP = Pattern.compile("(?is)<\\s*/?\\s*[a-z!][^>]*>");
    private static final Pattern SCRIPT_PROTOCOL = Pattern.compile("(?i)(javascript|vbscript)\\s*:");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    private InputSafety() { }

    public static String plainText(String value, String field, int maxLength, boolean required) {
        if (value == null || value.trim().isEmpty()) {
            if (required) throw new IllegalArgumentException(field + " is required");
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) throw new IllegalArgumentException(field + " is too long");
        if (MARKUP.matcher(normalized).find() || SCRIPT_PROTOCOL.matcher(normalized).find()
                || normalized.indexOf('<') >= 0 || normalized.indexOf('>') >= 0) {
            throw new IllegalArgumentException(field + " cannot contain HTML or script content");
        }
        return normalized;
    }

    public static String username(String value) {
        String normalized = plainText(value, "Username", 50, true);
        if (!USERNAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Username may contain only letters, numbers, dot, underscore, and hyphen");
        }
        return normalized.toLowerCase();
    }
}
