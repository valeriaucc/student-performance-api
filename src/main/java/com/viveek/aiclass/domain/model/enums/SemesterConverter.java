package com.viveek.aiclass.domain.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for Semester enum.
 * Converts between Semester enum and its database string representation.
 */
@Converter(autoApply = true)
public class SemesterConverter implements AttributeConverter<Semester, String> {

    @Override
    public String convertToDatabaseColumn(Semester attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public Semester convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return Semester.fromValue(dbData);
    }
}

