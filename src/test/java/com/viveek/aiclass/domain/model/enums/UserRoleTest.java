package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void values_ReturnsAllRoles() {
        UserRole[] roles = UserRole.values();
        
        assertThat(roles).hasSize(2);
        assertThat(roles).contains(UserRole.TEACHER, UserRole.STUDENT);
    }

    @Test
    void valueOf_Teacher_ReturnsTeacherRole() {
        UserRole role = UserRole.valueOf("TEACHER");
        
        assertThat(role).isEqualTo(UserRole.TEACHER);
    }

    @Test
    void valueOf_Student_ReturnsStudentRole() {
        UserRole role = UserRole.valueOf("STUDENT");
        
        assertThat(role).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void name_ReturnsCorrectName() {
        assertThat(UserRole.TEACHER.name()).isEqualTo("TEACHER");
        assertThat(UserRole.STUDENT.name()).isEqualTo("STUDENT");
    }

    @Test
    void toString_ReturnsName() {
        assertThat(UserRole.TEACHER.toString()).isEqualTo("TEACHER");
        assertThat(UserRole.STUDENT.toString()).isEqualTo("STUDENT");
    }

    @Test
    void ordinal_ReturnsCorrectOrder() {
        assertThat(UserRole.TEACHER.ordinal()).isEqualTo(0);
        assertThat(UserRole.STUDENT.ordinal()).isEqualTo(1);
    }

    @Test
    void compareTo_TeacherBeforeStudent() {
        assertThat(UserRole.TEACHER.compareTo(UserRole.STUDENT)).isLessThan(0);
        assertThat(UserRole.STUDENT.compareTo(UserRole.TEACHER)).isGreaterThan(0);
    }

    @Test
    void equals_SameRole_ReturnsTrue() {
        assertThat(UserRole.TEACHER).isEqualTo(UserRole.TEACHER);
        assertThat(UserRole.STUDENT).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void equals_DifferentRole_ReturnsFalse() {
        assertThat(UserRole.TEACHER).isNotEqualTo(UserRole.STUDENT);
    }
    
    @Test
    void getValue_ReturnsCorrectValue() {
        assertThat(UserRole.TEACHER.getValue()).isEqualTo("teacher");
        assertThat(UserRole.STUDENT.getValue()).isEqualTo("student");
    }
    
    @Test
    void fromValue_ValidValue_ReturnsCorrectEnum() {
        assertThat(UserRole.fromValue("teacher")).isEqualTo(UserRole.TEACHER);
        assertThat(UserRole.fromValue("student")).isEqualTo(UserRole.STUDENT);
    }
    
    @Test
    void fromValue_CaseInsensitive_ReturnsCorrectEnum() {
        assertThat(UserRole.fromValue("TEACHER")).isEqualTo(UserRole.TEACHER);
        assertThat(UserRole.fromValue("Student")).isEqualTo(UserRole.STUDENT);
    }
    
    @Test
    void fromValue_NullValue_ReturnsNull() {
        assertThat(UserRole.fromValue(null)).isNull();
    }
    
    @Test
    void fromValue_BlankValue_ReturnsNull() {
        assertThat(UserRole.fromValue("  ")).isNull();
    }
}

