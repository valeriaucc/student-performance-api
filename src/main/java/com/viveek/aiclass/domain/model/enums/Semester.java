package com.viveek.aiclass.domain.model.enums;

/**
 * Academic semester enumeration.
 * Represents the different semesters in an academic year.
 */
public enum Semester {
    SPRING("spring"),
    SUMMER("summer"),
    FALL("fall"),
    WINTER("winter");

    private final String value;

    Semester(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Semester fromValue(String value) {
        for (Semester semester : Semester.values()) {
            if (semester.value.equalsIgnoreCase(value)) {
                return semester;
            }
        }
        throw new IllegalArgumentException("Unknown semester: " + value);
    }
}

