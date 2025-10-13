package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.RecommendationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for AI Recommendation operations.
 */
public interface RecommendationService {

    RecommendationResponse createRecommendation(CreateRecommendationRequest request);

    RecommendationResponse getRecommendationById(UUID id);

    List<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId);

    List<RecommendationResponse> getRecommendationsByClassId(UUID classId);

    List<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience);

    void deleteRecommendation(UUID id);
}

