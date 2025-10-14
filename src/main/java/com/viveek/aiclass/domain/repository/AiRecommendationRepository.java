package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.AiRecommendation;
import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.recipient.id = :recipientId")
    List<AiRecommendation> findByRecipientId(@Param("recipientId") UUID recipientId);

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.recipient.id = :recipientId")
    Page<AiRecommendation> findByRecipientId(@Param("recipientId") UUID recipientId, Pageable pageable);

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.classEntity.id = :classId")
    List<AiRecommendation> findByClassEntityId(@Param("classId") UUID classId);

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.classEntity.id = :classId")
    Page<AiRecommendation> findByClassEntityId(@Param("classId") UUID classId, Pageable pageable);

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.audience = :audience")
    List<AiRecommendation> findByAudience(@Param("audience") RecommendationAudience audience);

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.audience = :audience")
    Page<AiRecommendation> findByAudience(@Param("audience") RecommendationAudience audience, Pageable pageable);

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

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM AiRecommendation r " +
           "JOIN r.classEntity c " +
           "WHERE r.id = :recommendationId " +
           "AND c.teacher.id = :teacherId")
    boolean isOwnedByTeacher(
            @Param("recommendationId") UUID recommendationId,
            @Param("teacherId") UUID teacherId
    );

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.recipient.id = :recipientId AND c.teacher.id = :teacherId")
    Page<AiRecommendation> findByRecipientIdAndTeacherId(
            @Param("recipientId") UUID recipientId,
            @Param("teacherId") UUID teacherId,
            Pageable pageable
    );

    @Query("SELECT r FROM AiRecommendation r " +
           "LEFT JOIN FETCH r.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH r.recipient " +
           "WHERE r.classEntity.id = :classId AND r.recipient.id = :recipientId")
    Page<AiRecommendation> findByClassEntityIdAndRecipientId(
            @Param("classId") UUID classId,
            @Param("recipientId") UUID recipientId,
            Pageable pageable
    );
}

