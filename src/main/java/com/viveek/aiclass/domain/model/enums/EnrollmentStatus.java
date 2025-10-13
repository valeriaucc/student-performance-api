package com.viveek.aiclass.domain.model.enums;

/**
 * Enrollment status enumeration.
 * Represents the current status of a student's enrollment in a class.
 */
public enum EnrollmentStatus {
    ACTIVE("active"),
    DROPPED("dropped"),
    COMPLETED("completed");

    private final String value;

    EnrollmentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EnrollmentStatus fromValue(String value) {
        for (EnrollmentStatus status : EnrollmentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown enrollment status: " + value);
    }
}

