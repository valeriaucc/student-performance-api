package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleConverterTest {

    private UserRoleConverter converter;

    @BeforeEach
    void setUp() {
        converter = new UserRoleConverter();
    }

    @Test
    void convertToDatabaseColumn_Teacher_ReturnsTeacher() {
        String result = converter.convertToDatabaseColumn(UserRole.TEACHER);
        
        assertThat(result).isEqualTo("teacher");
    }

    @Test
    void convertToDatabaseColumn_Student_ReturnsStudent() {
        String result = converter.convertToDatabaseColumn(UserRole.STUDENT);
        
        assertThat(result).isEqualTo("student");
    }

    @Test
    void convertToDatabaseColumn_Null_ReturnsNull() {
        String result = converter.convertToDatabaseColumn(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Teacher_ReturnsTeacherRole() {
        UserRole result = converter.convertToEntityAttribute("teacher");
        
        assertThat(result).isEqualTo(UserRole.TEACHER);
    }

    @Test
    void convertToEntityAttribute_Student_ReturnsStudentRole() {
        UserRole result = converter.convertToEntityAttribute("student");
        
        assertThat(result).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void convertToEntityAttribute_CaseInsensitive_ReturnsCorrectRole() {
        assertThat(converter.convertToEntityAttribute("TEACHER")).isEqualTo(UserRole.TEACHER);
        assertThat(converter.convertToEntityAttribute("Student")).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void convertToEntityAttribute_Null_ReturnsNull() {
        UserRole result = converter.convertToEntityAttribute(null);
        
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_Empty_ReturnsNull() {
        UserRole result = converter.convertToEntityAttribute("");
        
        assertThat(result).isNull();
    }

    @Test
    void roundTrip_Teacher_MaintainsValue() {
        String dbValue = converter.convertToDatabaseColumn(UserRole.TEACHER);
        UserRole entityValue = converter.convertToEntityAttribute(dbValue);
        
        assertThat(entityValue).isEqualTo(UserRole.TEACHER);
    }

    @Test
    void roundTrip_Student_MaintainsValue() {
        String dbValue = converter.convertToDatabaseColumn(UserRole.STUDENT);
        UserRole entityValue = converter.convertToEntityAttribute(dbValue);
        
        assertThat(entityValue).isEqualTo(UserRole.STUDENT);
    }
}

