package com.viveek.aiclass.domain.model;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI Recommendation entity representing AI-generated recommendations for users.
 * Maps to the 'ai_recommendations' table in the database.
 */
@Entity
@Table(name = "ai_recommendations")
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

    @Column(name = "audience", nullable = false)
    private RecommendationAudience audience;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}

