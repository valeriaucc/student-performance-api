package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterTest {

    @Test
    void values_ReturnsAllSemesters() {
        Semester[] semesters = Semester.values();
        
        assertThat(semesters).hasSize(4);
        assertThat(semesters).contains(
            Semester.SPRING, 
            Semester.SUMMER, 
            Semester.FALL, 
            Semester.WINTER
        );
    }

    @Test
    void valueOf_Spring_ReturnsSpring() {
        Semester semester = Semester.valueOf("SPRING");
        
        assertThat(semester).isEqualTo(Semester.SPRING);
    }

    @Test
    void valueOf_AllSemesters_ReturnsCorrectValues() {
        assertThat(Semester.valueOf("SPRING")).isEqualTo(Semester.SPRING);
        assertThat(Semester.valueOf("SUMMER")).isEqualTo(Semester.SUMMER);
        assertThat(Semester.valueOf("FALL")).isEqualTo(Semester.FALL);
        assertThat(Semester.valueOf("WINTER")).isEqualTo(Semester.WINTER);
    }

    @Test
    void name_ReturnsCorrectName() {
        assertThat(Semester.SPRING.name()).isEqualTo("SPRING");
        assertThat(Semester.SUMMER.name()).isEqualTo("SUMMER");
        assertThat(Semester.FALL.name()).isEqualTo("FALL");
        assertThat(Semester.WINTER.name()).isEqualTo("WINTER");
    }

    @Test
    void toString_ReturnsName() {
        assertThat(Semester.SPRING.toString()).isEqualTo("SPRING");
        assertThat(Semester.SUMMER.toString()).isEqualTo("SUMMER");
    }

    @Test
    void ordinal_ReturnsCorrectOrder() {
        assertThat(Semester.SPRING.ordinal()).isEqualTo(0);
        assertThat(Semester.SUMMER.ordinal()).isEqualTo(1);
        assertThat(Semester.FALL.ordinal()).isEqualTo(2);
        assertThat(Semester.WINTER.ordinal()).isEqualTo(3);
    }

    @Test
    void compareTo_WorksCorrectly() {
        assertThat(Semester.SPRING.compareTo(Semester.SUMMER)).isLessThan(0);
        assertThat(Semester.FALL.compareTo(Semester.SPRING)).isGreaterThan(0);
        assertThat(Semester.WINTER.compareTo(Semester.WINTER)).isEqualTo(0);
    }

    @Test
    void equals_SameSemester_ReturnsTrue() {
        assertThat(Semester.SPRING).isEqualTo(Semester.SPRING);
        assertThat(Semester.FALL).isEqualTo(Semester.FALL);
    }

    @Test
    void equals_DifferentSemester_ReturnsFalse() {
        assertThat(Semester.SPRING).isNotEqualTo(Semester.FALL);
        assertThat(Semester.SUMMER).isNotEqualTo(Semester.WINTER);
    }
    
    @Test
    void getValue_ReturnsCorrectValue() {
        assertThat(Semester.SPRING.getValue()).isEqualTo("spring");
        assertThat(Semester.SUMMER.getValue()).isEqualTo("summer");
        assertThat(Semester.FALL.getValue()).isEqualTo("fall");
        assertThat(Semester.WINTER.getValue()).isEqualTo("winter");
    }
    
    @Test
    void fromValue_ValidValue_ReturnsCorrectEnum() {
        assertThat(Semester.fromValue("spring")).isEqualTo(Semester.SPRING);
        assertThat(Semester.fromValue("summer")).isEqualTo(Semester.SUMMER);
        assertThat(Semester.fromValue("fall")).isEqualTo(Semester.FALL);
        assertThat(Semester.fromValue("winter")).isEqualTo(Semester.WINTER);
    }
    
    @Test
    void fromValue_CaseInsensitive_ReturnsCorrectEnum() {
        assertThat(Semester.fromValue("SPRING")).isEqualTo(Semester.SPRING);
        assertThat(Semester.fromValue("Fall")).isEqualTo(Semester.FALL);
    }
}

