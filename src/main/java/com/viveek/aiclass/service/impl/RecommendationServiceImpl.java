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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of RecommendationService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final AiRecommendationRepository recommendationRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @Override
    public RecommendationResponse createRecommendation(CreateRecommendationRequest request) {
        log.info("Creating recommendation for classId={}, recipientId={}, audience={}", 
                 request.getClassId(), request.getRecipientId(), request.getAudience());
        
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

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
        log.debug("Recommendation created successfully: id={}", savedRecommendation.getId());
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
        log.debug("Fetching recommendations by recipient: recipientId={} (non-paginated)", recipientId);
        return recommendationRepository.findByRecipientId(recipientId).stream()
                .map(EntityMapper::toRecommendationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId, Pageable pageable) {
        log.debug("Fetching recommendations by recipient with pagination: recipientId={}, page={}, size={}", 
                  recipientId, pageable.getPageNumber(), pageable.getPageSize());
        return recommendationRepository.findByRecipientId(recipientId, pageable)
                .map(EntityMapper::toRecommendationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendationsByClassId(UUID classId) {
        log.debug("Fetching recommendations by class: classId={} (non-paginated)", classId);
        return recommendationRepository.findByClassEntityId(classId).stream()
                .map(EntityMapper::toRecommendationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByClassId(UUID classId, Pageable pageable) {
        log.debug("Fetching recommendations by class with pagination: classId={}, page={}, size={}", 
                  classId, pageable.getPageNumber(), pageable.getPageSize());
        return recommendationRepository.findByClassEntityId(classId, pageable)
                .map(EntityMapper::toRecommendationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience) {
        log.debug("Fetching recommendations by audience: audience={} (non-paginated)", audience);
        return recommendationRepository.findByAudience(audience).stream()
                .map(EntityMapper::toRecommendationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience, Pageable pageable) {
        log.debug("Fetching recommendations by audience with pagination: audience={}, page={}, size={}", 
                  audience, pageable.getPageNumber(), pageable.getPageSize());
        return recommendationRepository.findByAudience(audience, pageable)
                .map(EntityMapper::toRecommendationResponse);
    }

    @Override
    public void deleteRecommendation(UUID id) {
        log.info("Deleting recommendation: id={}", id);
        if (!recommendationRepository.existsById(id)) {
            log.warn("Recommendation deletion failed: recommendation not found - id={}", id);
            throw new ResourceNotFoundException("Recommendation", "id", id);
        }
        recommendationRepository.deleteById(id);
        log.debug("Recommendation deleted successfully: id={}", id);
    }
}

