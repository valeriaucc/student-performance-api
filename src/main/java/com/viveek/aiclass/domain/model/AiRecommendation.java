package com.viveek.aiclass.domain.model;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import java.util.HashMap;
import java.util.Map;

/**
 * AI Recommendation entity representing AI-generated recommendations for users.
 * Maps to the 'ai_recommendations' table in the database.
 * 
 * Recommendations can be:
 * - General recommendations (grade = null): Linked to class/student, not specific to an assessment
 * - Assessment-specific recommendations (grade != null): Generated for a specific grade/assessment
 * 
 * Uses soft delete: DELETE operations will set deleted_at instead of removing the record.
 * Soft-deleted records are automatically filtered from queries via @Where annotation.
 */
@Entity
@Table(name = "ai_recommendations")
@SQLDelete(sql = "UPDATE ai_recommendations SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = true)
    private Grade grade;

    @Column(name = "audience", nullable = false)
    private RecommendationAudience audience;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}

