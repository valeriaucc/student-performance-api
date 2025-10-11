package com.viveek.aiclass.domain.model.enums;

/**
 * Recommendation audience enumeration.
 * Defines who the AI recommendation is intended for.
 */
public enum RecommendationAudience {
    TEACHER("teacher"),
    STUDENT("student");

    private final String value;

    RecommendationAudience(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RecommendationAudience fromValue(String value) {
        for (RecommendationAudience audience : RecommendationAudience.values()) {
            if (audience.value.equalsIgnoreCase(value)) {
                return audience;
            }
        }
        throw new IllegalArgumentException("Unknown recommendation audience: " + value);
    }
}

