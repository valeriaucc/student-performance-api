package com.viveek.aiclass.domain.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for RecommendationAudience enum.
 * Converts between RecommendationAudience enum and its database string representation.
 */
@Converter(autoApply = true)
public class RecommendationAudienceConverter implements AttributeConverter<RecommendationAudience, String> {

    @Override
    public String convertToDatabaseColumn(RecommendationAudience attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public RecommendationAudience convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return RecommendationAudience.fromValue(dbData);
    }
}

