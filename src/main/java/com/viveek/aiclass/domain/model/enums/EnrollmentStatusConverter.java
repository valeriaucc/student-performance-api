package com.viveek.aiclass.domain.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for EnrollmentStatus enum.
 * Converts between EnrollmentStatus enum and its database string representation.
 */
@Converter(autoApply = true)
public class EnrollmentStatusConverter implements AttributeConverter<EnrollmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(EnrollmentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public EnrollmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return EnrollmentStatus.fromValue(dbData);
    }
}

