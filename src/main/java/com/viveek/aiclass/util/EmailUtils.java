package com.viveek.aiclass.util;

/**
 * Utility class for email operations.
 * 
 * Provides standardized email processing to ensure consistency across the application.
 * All emails are normalized to lowercase and trimmed of whitespace.
 */
public final class EmailUtils {

    private EmailUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Normalizes an email address by converting to lowercase and trimming whitespace.
     * 
     * This ensures that:
     * - "User@Example.com" becomes "user@example.com"
     * - " user@example.com " becomes "user@example.com"
     * - Case-insensitive email lookups work correctly
     * - Consistent storage in database
     * 
     * @param email the email address to normalize
     * @return normalized email (lowercase, trimmed), or null if input is null
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Validates that an email is not null, not blank, and properly formatted.
     * 
     * @param email the email to validate
     * @return true if email is valid (non-null, non-blank, contains @)
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        // Basic validation - contains @ and has characters before/after
        String normalized = normalizeEmail(email);
        return normalized.contains("@") && 
               normalized.indexOf('@') > 0 && 
               normalized.indexOf('@') < normalized.length() - 1;
    }
}

