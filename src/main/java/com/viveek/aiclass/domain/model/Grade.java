package com.viveek.aiclass.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Grade entity representing a student's grade/score in a class.
 * Maps to the 'grades' table in the database.
 * 
 * Uses soft delete: DELETE operations will set deleted_at instead of removing the record.
 * Soft-deleted records are automatically filtered from queries via @Where annotation.
 */
@Entity
@Table(name = "grades")
@SQLDelete(sql = "UPDATE grades SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "assessment_kind", nullable = false)
    private String assessmentKind;

    @Column(name = "assessment_name")
    private String assessmentName;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "max_score")
    private BigDecimal maxScore;

    @Column(name = "graded_at")
    private ZonedDateTime gradedAt;
}

