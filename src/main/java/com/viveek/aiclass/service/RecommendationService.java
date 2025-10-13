package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for AI Recommendation operations.
 */
public interface RecommendationService {

    RecommendationResponse createRecommendation(CreateRecommendationRequest request);

    RecommendationResponse getRecommendationById(UUID id);

    List<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId);

    Page<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId, Pageable pageable);

    List<RecommendationResponse> getRecommendationsByClassId(UUID classId);

    Page<RecommendationResponse> getRecommendationsByClassId(UUID classId, Pageable pageable);

    List<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience);

    Page<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience, Pageable pageable);

    void deleteRecommendation(UUID id);
}

