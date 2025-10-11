package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.AiRecommendation;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.domain.repository.AiRecommendationRepository;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of RecommendationService.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final AiRecommendationRepository recommendationRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @Override
    public RecommendationResponse createRecommendation(CreateRecommendationRequest request) {
        // Validate class exists
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        // Validate recipient exists
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getRecipientId()));

        AiRecommendation recommendation = AiRecommendation.builder()
                .classEntity(classEntity)
                .recipient(recipient)
                .audience(request.getAudience())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .build();

        AiRecommendation savedRecommendation = recommendationRepository.save(recommendation);
        return EntityMapper.toRecommendationResponse(savedRecommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendationById(UUID id) {
        AiRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", "id", id));
        return EntityMapper.toRecommendationResponse(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId) {
        return recommendationRepository.findByRecipientId(recipientId).stream()
                .map(EntityMapper::toRecommendationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendationsByClassId(UUID classId) {
        return recommendationRepository.findByClassEntityId(classId).stream()
                .map(EntityMapper::toRecommendationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience) {
        return recommendationRepository.findByAudience(audience).stream()
                .map(EntityMapper::toRecommendationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRecommendation(UUID id) {
        if (!recommendationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recommendation", "id", id);
        }
        recommendationRepository.deleteById(id);
    }
}

