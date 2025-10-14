package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterConverterTest {

    private SemesterConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SemesterConverter();
    }

    @Test
    void convertToDatabaseColumn_Spring_ReturnsSpring() {
        String result = converter.convertToDatabaseColumn(Semester.SPRING);
        
        assertThat(result).isEqualTo("spring");
    }

    @Test
    void convertToDatabaseColumn_AllSemesters_ReturnsCorrectValues() {
        assertThat(converter.convertToDatabaseColumn(Semester.SPRING)).isEqualTo("spring");
        assertThat(converter.convertToDatabaseColumn(Semester.SUMMER)).isEqualTo("summer");
        assertThat(converter.convertToDatabaseColumn(Semester.FALL)).isEqualTo("fall");
        assertThat(converter.convertToDatabaseColumn(Semester.WINTER)).isEqualTo("winter");
    }

    @Test
    void convertToDatabaseColumn_Null_ReturnsNull() {
        String result = converter.convertToDatabaseColumn(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Spring_ReturnsSpring() {
        Semester result = converter.convertToEntityAttribute("spring");
        
        assertThat(result).isEqualTo(Semester.SPRING);
    }

    @Test
    void convertToEntityAttribute_AllSemesters_ReturnsCorrectValues() {
        assertThat(converter.convertToEntityAttribute("spring")).isEqualTo(Semester.SPRING);
        assertThat(converter.convertToEntityAttribute("summer")).isEqualTo(Semester.SUMMER);
        assertThat(converter.convertToEntityAttribute("fall")).isEqualTo(Semester.FALL);
        assertThat(converter.convertToEntityAttribute("winter")).isEqualTo(Semester.WINTER);
    }

    @Test
    void convertToEntityAttribute_CaseInsensitive_ReturnsCorrectSemester() {
        assertThat(converter.convertToEntityAttribute("SPRING")).isEqualTo(Semester.SPRING);
        assertThat(converter.convertToEntityAttribute("Fall")).isEqualTo(Semester.FALL);
    }

    @Test
    void convertToEntityAttribute_Null_ReturnsNull() {
        Semester result = converter.convertToEntityAttribute(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Empty_ReturnsNull() {
        Semester result = converter.convertToEntityAttribute("");
        
        assertThat(result).isNull();
    }

    @Test
    void roundTrip_Spring_MaintainsValue() {
        String dbValue = converter.convertToDatabaseColumn(Semester.SPRING);
        Semester entityValue = converter.convertToEntityAttribute(dbValue);
        
        assertThat(entityValue).isEqualTo(Semester.SPRING);
    }

    @Test
    void roundTrip_AllSemesters_MaintainValue() {
        for (Semester semester : Semester.values()) {
            String dbValue = converter.convertToDatabaseColumn(semester);
            Semester entityValue = converter.convertToEntityAttribute(dbValue);
            assertThat(entityValue).isEqualTo(semester);
        }
    }
}

