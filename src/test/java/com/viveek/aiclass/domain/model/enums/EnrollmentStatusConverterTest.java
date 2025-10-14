package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentStatusConverterTest {

    private EnrollmentStatusConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EnrollmentStatusConverter();
    }

    @Test
    void convertToDatabaseColumn_Active_ReturnsActive() {
        String result = converter.convertToDatabaseColumn(EnrollmentStatus.ACTIVE);
        
        assertThat(result).isEqualTo("active");
    }

    @Test
    void convertToDatabaseColumn_AllStatuses_ReturnsCorrectValues() {
        assertThat(converter.convertToDatabaseColumn(EnrollmentStatus.ACTIVE)).isEqualTo("active");
        assertThat(converter.convertToDatabaseColumn(EnrollmentStatus.DROPPED)).isEqualTo("dropped");
        assertThat(converter.convertToDatabaseColumn(EnrollmentStatus.COMPLETED)).isEqualTo("completed");
    }

    @Test
    void convertToDatabaseColumn_Null_ReturnsNull() {
        String result = converter.convertToDatabaseColumn(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Active_ReturnsActive() {
        EnrollmentStatus result = converter.convertToEntityAttribute("active");
        
        assertThat(result).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void convertToEntityAttribute_AllStatuses_ReturnsCorrectValues() {
        assertThat(converter.convertToEntityAttribute("active")).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(converter.convertToEntityAttribute("dropped")).isEqualTo(EnrollmentStatus.DROPPED);
        assertThat(converter.convertToEntityAttribute("completed")).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void convertToEntityAttribute_CaseInsensitive_ReturnsCorrectStatus() {
        assertThat(converter.convertToEntityAttribute("ACTIVE")).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(converter.convertToEntityAttribute("Dropped")).isEqualTo(EnrollmentStatus.DROPPED);
    }

    @Test
    void convertToEntityAttribute_Null_ReturnsNull() {
        EnrollmentStatus result = converter.convertToEntityAttribute(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Empty_ReturnsNull() {
        EnrollmentStatus result = converter.convertToEntityAttribute("");
        
        assertThat(result).isNull();
    }

    @Test
    void roundTrip_Active_MaintainsValue() {
        String dbValue = converter.convertToDatabaseColumn(EnrollmentStatus.ACTIVE);
        EnrollmentStatus entityValue = converter.convertToEntityAttribute(dbValue);
        
        assertThat(entityValue).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void roundTrip_AllStatuses_MaintainValue() {
        for (EnrollmentStatus status : EnrollmentStatus.values()) {
            String dbValue = converter.convertToDatabaseColumn(status);
            EnrollmentStatus entityValue = converter.convertToEntityAttribute(dbValue);
            assertThat(entityValue).isEqualTo(status);
        }
    }
}

