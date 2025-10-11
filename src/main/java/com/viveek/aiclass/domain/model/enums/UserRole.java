package com.viveek.aiclass.domain.model.enums;

/**
 * User role enumeration.
 * Defines the possible roles a user can have in the system.
 */
public enum UserRole {
    TEACHER("teacher"),
    STUDENT("student");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + value);
    }
}

