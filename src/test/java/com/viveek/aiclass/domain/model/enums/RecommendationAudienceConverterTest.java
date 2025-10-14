package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationAudienceConverterTest {

    private RecommendationAudienceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new RecommendationAudienceConverter();
    }

    @Test
    void convertToDatabaseColumn_Teacher_ReturnsTeacher() {
        String result = converter.convertToDatabaseColumn(RecommendationAudience.TEACHER);
        
        assertThat(result).isEqualTo("teacher");
    }

    @Test
    void convertToDatabaseColumn_Student_ReturnsStudent() {
        String result = converter.convertToDatabaseColumn(RecommendationAudience.STUDENT);
        
        assertThat(result).isEqualTo("student");
    }

    @Test
    void convertToDatabaseColumn_Null_ReturnsNull() {
        String result = converter.convertToDatabaseColumn(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Teacher_ReturnsTeacherAudience() {
        RecommendationAudience result = converter.convertToEntityAttribute("teacher");
        
        assertThat(result).isEqualTo(RecommendationAudience.TEACHER);
    }

    @Test
    void convertToEntityAttribute_Student_ReturnsStudentAudience() {
        RecommendationAudience result = converter.convertToEntityAttribute("student");
        
        assertThat(result).isEqualTo(RecommendationAudience.STUDENT);
    }

    @Test
    void convertToEntityAttribute_CaseInsensitive_ReturnsCorrectAudience() {
        assertThat(converter.convertToEntityAttribute("TEACHER")).isEqualTo(RecommendationAudience.TEACHER);
        assertThat(converter.convertToEntityAttribute("Student")).isEqualTo(RecommendationAudience.STUDENT);
    }

    @Test
    void convertToEntityAttribute_Null_ReturnsNull() {
        RecommendationAudience result = converter.convertToEntityAttribute(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Empty_ReturnsNull() {
        RecommendationAudience result = converter.convertToEntityAttribute("");
        
        assertThat(result).isNull();
    }

    @Test
    void roundTrip_Teacher_MaintainsValue() {
        String dbValue = converter.convertToDatabaseColumn(RecommendationAudience.TEACHER);
        RecommendationAudience entityValue = converter.convertToEntityAttribute(dbValue);
        
        assertThat(entityValue).isEqualTo(RecommendationAudience.TEACHER);
    }

    @Test
    void roundTrip_Student_MaintainsValue() {
        String dbValue = converter.convertToDatabaseColumn(RecommendationAudience.STUDENT);
        RecommendationAudience entityValue = converter.convertToEntityAttribute(dbValue);
        
        assertThat(entityValue).isEqualTo(RecommendationAudience.STUDENT);
    }
}

