package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationAudienceTest {

    @Test
    void values_ReturnsAllAudiences() {
        RecommendationAudience[] audiences = RecommendationAudience.values();
        
        assertThat(audiences).hasSize(2);
        assertThat(audiences).contains(
            RecommendationAudience.STUDENT,
            RecommendationAudience.TEACHER
        );
    }

    @Test
    void valueOf_Student_ReturnsStudent() {
        RecommendationAudience audience = RecommendationAudience.valueOf("STUDENT");
        
        assertThat(audience).isEqualTo(RecommendationAudience.STUDENT);
    }

    @Test
    void valueOf_AllAudiences_ReturnsCorrectValues() {
        assertThat(RecommendationAudience.valueOf("STUDENT")).isEqualTo(RecommendationAudience.STUDENT);
        assertThat(RecommendationAudience.valueOf("TEACHER")).isEqualTo(RecommendationAudience.TEACHER);
    }

    @Test
    void name_ReturnsCorrectName() {
        assertThat(RecommendationAudience.STUDENT.name()).isEqualTo("STUDENT");
        assertThat(RecommendationAudience.TEACHER.name()).isEqualTo("TEACHER");
    }

    @Test
    void toString_ReturnsName() {
        assertThat(RecommendationAudience.STUDENT.toString()).isEqualTo("STUDENT");
        assertThat(RecommendationAudience.TEACHER.toString()).isEqualTo("TEACHER");
    }

    @Test
    void ordinal_ReturnsCorrectOrder() {
        assertThat(RecommendationAudience.TEACHER.ordinal()).isEqualTo(0);
        assertThat(RecommendationAudience.STUDENT.ordinal()).isEqualTo(1);
    }

    @Test
    void compareTo_WorksCorrectly() {
        assertThat(RecommendationAudience.TEACHER.compareTo(RecommendationAudience.STUDENT)).isLessThan(0);
        assertThat(RecommendationAudience.STUDENT.compareTo(RecommendationAudience.TEACHER)).isGreaterThan(0);
        assertThat(RecommendationAudience.TEACHER.compareTo(RecommendationAudience.TEACHER)).isEqualTo(0);
    }

    @Test
    void equals_SameAudience_ReturnsTrue() {
        assertThat(RecommendationAudience.STUDENT).isEqualTo(RecommendationAudience.STUDENT);
        assertThat(RecommendationAudience.TEACHER).isEqualTo(RecommendationAudience.TEACHER);
    }

    @Test
    void equals_DifferentAudience_ReturnsFalse() {
        assertThat(RecommendationAudience.STUDENT).isNotEqualTo(RecommendationAudience.TEACHER);
    }
    
    @Test
    void getValue_ReturnsCorrectValue() {
        assertThat(RecommendationAudience.TEACHER.getValue()).isEqualTo("teacher");
        assertThat(RecommendationAudience.STUDENT.getValue()).isEqualTo("student");
    }
    
    @Test
    void fromValue_ValidValue_ReturnsCorrectEnum() {
        assertThat(RecommendationAudience.fromValue("teacher")).isEqualTo(RecommendationAudience.TEACHER);
        assertThat(RecommendationAudience.fromValue("student")).isEqualTo(RecommendationAudience.STUDENT);
    }
    
    @Test
    void fromValue_CaseInsensitive_ReturnsCorrectEnum() {
        assertThat(RecommendationAudience.fromValue("TEACHER")).isEqualTo(RecommendationAudience.TEACHER);
        assertThat(RecommendationAudience.fromValue("Student")).isEqualTo(RecommendationAudience.STUDENT);
    }
    
    @Test
    void fromValue_NullValue_ReturnsNull() {
        assertThat(RecommendationAudience.fromValue(null)).isNull();
    }
    
    @Test
    void fromValue_BlankValue_ReturnsNull() {
        assertThat(RecommendationAudience.fromValue("  ")).isNull();
    }
}

