package com.viveek.aiclass.domain.model;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Enrollment entity representing a student's enrollment in a class.
 * Maps to the 'enrollments' table in the database.
 */
@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "enrolled_at", nullable = false)
    private ZonedDateTime enrolledAt;

    @PrePersist
    protected void onEnrollmentCreate() {
        super.onCreate();
        if (enrolledAt == null) {
            enrolledAt = ZonedDateTime.now();
        }
        if (status == null) {
            status = EnrollmentStatus.ACTIVE;
        }
    }
}

