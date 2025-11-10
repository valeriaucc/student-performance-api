package com.viveek.aiclass.domain.model;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.ZonedDateTime;

/**
 * Enrollment entity representing a student's enrollment in a class.
 * Maps to the 'enrollments' table in the database.
 * 
 * Uses soft delete: DELETE operations will set deleted_at instead of removing the record.
 * Soft-deleted records are automatically filtered from queries via @Where annotation.
 */
@Entity
@Table(name = "enrollments")
@SQLDelete(sql = "UPDATE enrollments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
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

