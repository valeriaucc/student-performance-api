package com.viveek.aiclass.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentStatusTest {

    @Test
    void values_ReturnsAllStatuses() {
        EnrollmentStatus[] statuses = EnrollmentStatus.values();
        
        assertThat(statuses).hasSize(3);
        assertThat(statuses).contains(
            EnrollmentStatus.ACTIVE,
            EnrollmentStatus.DROPPED,
            EnrollmentStatus.COMPLETED
        );
    }

    @Test
    void valueOf_Active_ReturnsActive() {
        EnrollmentStatus status = EnrollmentStatus.valueOf("ACTIVE");
        
        assertThat(status).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void valueOf_AllStatuses_ReturnsCorrectValues() {
        assertThat(EnrollmentStatus.valueOf("ACTIVE")).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(EnrollmentStatus.valueOf("DROPPED")).isEqualTo(EnrollmentStatus.DROPPED);
        assertThat(EnrollmentStatus.valueOf("COMPLETED")).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void name_ReturnsCorrectName() {
        assertThat(EnrollmentStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        assertThat(EnrollmentStatus.DROPPED.name()).isEqualTo("DROPPED");
        assertThat(EnrollmentStatus.COMPLETED.name()).isEqualTo("COMPLETED");
    }

    @Test
    void toString_ReturnsName() {
        assertThat(EnrollmentStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(EnrollmentStatus.DROPPED.toString()).isEqualTo("DROPPED");
    }

    @Test
    void ordinal_ReturnsCorrectOrder() {
        assertThat(EnrollmentStatus.ACTIVE.ordinal()).isEqualTo(0);
        assertThat(EnrollmentStatus.DROPPED.ordinal()).isEqualTo(1);
        assertThat(EnrollmentStatus.COMPLETED.ordinal()).isEqualTo(2);
    }

    @Test
    void compareTo_WorksCorrectly() {
        assertThat(EnrollmentStatus.ACTIVE.compareTo(EnrollmentStatus.DROPPED)).isLessThan(0);
        assertThat(EnrollmentStatus.COMPLETED.compareTo(EnrollmentStatus.ACTIVE)).isGreaterThan(0);
        assertThat(EnrollmentStatus.ACTIVE.compareTo(EnrollmentStatus.ACTIVE)).isEqualTo(0);
    }

    @Test
    void equals_SameStatus_ReturnsTrue() {
        assertThat(EnrollmentStatus.ACTIVE).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(EnrollmentStatus.COMPLETED).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void equals_DifferentStatus_ReturnsFalse() {
        assertThat(EnrollmentStatus.ACTIVE).isNotEqualTo(EnrollmentStatus.DROPPED);
        assertThat(EnrollmentStatus.DROPPED).isNotEqualTo(EnrollmentStatus.COMPLETED);
    }
    
    @Test
    void getValue_ReturnsCorrectValue() {
        assertThat(EnrollmentStatus.ACTIVE.getValue()).isEqualTo("active");
        assertThat(EnrollmentStatus.DROPPED.getValue()).isEqualTo("dropped");
        assertThat(EnrollmentStatus.COMPLETED.getValue()).isEqualTo("completed");
    }
    
    @Test
    void fromValue_ValidValue_ReturnsCorrectEnum() {
        assertThat(EnrollmentStatus.fromValue("active")).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(EnrollmentStatus.fromValue("dropped")).isEqualTo(EnrollmentStatus.DROPPED);
        assertThat(EnrollmentStatus.fromValue("completed")).isEqualTo(EnrollmentStatus.COMPLETED);
    }
    
    @Test
    void fromValue_CaseInsensitive_ReturnsCorrectEnum() {
        assertThat(EnrollmentStatus.fromValue("ACTIVE")).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(EnrollmentStatus.fromValue("Dropped")).isEqualTo(EnrollmentStatus.DROPPED);
    }
}

