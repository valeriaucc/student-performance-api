package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for AI Recommendation operations.
 * All list operations use pagination for better performance and consistency.
 */
public interface RecommendationService {

    RecommendationResponse createRecommendation(CreateRecommendationRequest request);

    RecommendationResponse getRecommendationById(UUID id);

    /**
     * Get recommendations by recipient ID with pagination and authorization.
     * Students can only view their own recommendations, teachers can view recommendations for students in their classes.
     */
    Page<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId, Pageable pageable);

    /**
     * Get recommendations by class ID with pagination and authorization.
     * Teachers must own the class, students can only see their own recommendations within the class.
     */
    Page<RecommendationResponse> getRecommendationsByClassId(UUID classId, Pageable pageable);

    /**
     * Get recommendations by audience with pagination.
     */
    Page<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience, Pageable pageable);

    void deleteRecommendation(UUID id);
}

