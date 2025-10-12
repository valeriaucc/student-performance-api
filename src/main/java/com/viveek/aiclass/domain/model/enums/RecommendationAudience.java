package com.viveek.aiclass.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Recommendation audience enumeration.
 * Defines who the AI recommendation is intended for.
 * 
 * Database values: 'teacher', 'student' (lowercase)
 * JSON serialization: 'teacher', 'student' (lowercase)
 */
public enum RecommendationAudience {
    TEACHER("teacher"),
    STUDENT("student");

    private final String value;

    RecommendationAudience(String value) {
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
    public static RecommendationAudience fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (RecommendationAudience audience : RecommendationAudience.values()) {
            if (audience.value.equalsIgnoreCase(value)) {
                return audience;
            }
        }
        throw new IllegalArgumentException("Unknown recommendation audience: " + value);
    }
}

