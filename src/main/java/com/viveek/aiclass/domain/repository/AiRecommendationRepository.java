package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.AiRecommendation;
import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AiRecommendation entity operations.
 */
@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, UUID> {

    List<AiRecommendation> findByRecipientId(UUID recipientId);

    List<AiRecommendation> findByClassEntityId(UUID classId);

    List<AiRecommendation> findByAudience(RecommendationAudience audience);

    @Query("SELECT r FROM AiRecommendation r WHERE r.classEntity.id = :classId AND r.audience = :audience")
    List<AiRecommendation> findByClassAndAudience(
            @Param("classId") UUID classId,
            @Param("audience") RecommendationAudience audience
    );

    @Query("SELECT r FROM AiRecommendation r WHERE r.recipient.id = :recipientId AND r.classEntity.id = :classId")
    List<AiRecommendation> findByRecipientAndClass(
            @Param("recipientId") UUID recipientId,
            @Param("classId") UUID classId
    );
}

