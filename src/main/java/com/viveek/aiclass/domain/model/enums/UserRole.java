package com.viveek.aiclass.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User role enumeration.
 * Defines the possible roles a user can have in the system.
 * 
 * Database values: 'teacher', 'student' (lowercase)
 * JSON serialization: 'teacher', 'student' (lowercase)
 */
public enum UserRole {
    TEACHER("teacher"),
    STUDENT("student");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    /**
     * Get the database/JSON value (lowercase).
     * Used by JPA converter and JSON serialization.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Create enum from database/JSON value (case-insensitive).
     */
    public static UserRole fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + value);
    }
}

