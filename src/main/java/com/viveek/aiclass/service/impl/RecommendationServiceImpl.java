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
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.RecommendationMapper;
import com.viveek.aiclass.security.AuthenticatedUser;
import com.viveek.aiclass.security.SecurityContextHelper;
import com.viveek.aiclass.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    private final EnrollmentRepository enrollmentRepository;
    private final RecommendationMapper recommendationMapper;

    @Override
    public RecommendationResponse createRecommendation(CreateRecommendationRequest request) {
        log.info("Creating recommendation for classId={}, recipientId={}, audience={}", 
                 request.getClassId(), request.getRecipientId(), request.getAudience());
        
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        // ✅ AUTHORIZATION: Verify the current user is the teacher of this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Recommendation creation denied: user {} is not the teacher of class {}", 
                     currentUser.getUserId(), request.getClassId());
            throw new AccessDeniedException("You can only create recommendations for your classes");
        }

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
        return recommendationMapper.toResponse(savedRecommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendationById(UUID id) {
        AiRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", "id", id));
        
        // ✅ AUTHORIZATION: Verify access based on role
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isTeacher()) {
            // Teachers can only view recommendations for their own classes
            if (!recommendation.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendation access denied: teacher {} tried to access recommendation {} for class owned by teacher {}", 
                         currentUser.getUserId(), id, recommendation.getClassEntity().getTeacher().getId());
                throw new AccessDeniedException("You can only view recommendations for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can only view their own recommendations
            if (!recommendation.getRecipient().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendation access denied: student {} tried to access recommendation {} for recipient {}", 
                         currentUser.getUserId(), id, recommendation.getRecipient().getId());
                throw new AccessDeniedException("You can only view your own recommendations");
            }
        }
        
        return recommendationMapper.toResponse(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId, Pageable pageable) {
        log.debug("Fetching recommendations by recipient with pagination: recipientId={}, page={}, size={}", 
                  recipientId, pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Verify access rights
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isStudent()) {
            // Students can only view their own recommendations
            if (!recipientId.equals(currentUser.getUserId())) {
                log.warn("Recommendations access denied: student {} tried to access recommendations for recipient {}", 
                         currentUser.getUserId(), recipientId);
                throw new AccessDeniedException("You can only view your own recommendations");
            }
        } else if (currentUser.isTeacher()) {
            return recommendationRepository.findByRecipientIdAndTeacherId(
                    recipientId,
                    currentUser.getUserId(),
                    pageable
            ).map(recommendationMapper::toResponse);
        }
        
        return recommendationRepository.findByRecipientId(recipientId, pageable)
                .map(recommendationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByClassId(UUID classId, Pageable pageable) {
        log.debug("Fetching recommendations by class with pagination: classId={}, page={}, size={}", 
                  classId, pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Verify the teacher owns this class or student is enrolled
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));
        
        if (currentUser.isTeacher()) {
            if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendations access denied: teacher {} tried to access recommendations for class owned by teacher {}", 
                         currentUser.getUserId(), classEntity.getTeacher().getId());
                throw new AccessDeniedException("You can only view recommendations for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can only view recommendations for classes they're enrolled in
            if (!enrollmentRepository.isStudentEnrolledInClass(currentUser.getUserId(), classId)) {
                log.warn("Recommendations access denied: student {} tried to access recommendations for class without enrollment", 
                         currentUser.getUserId());
                throw new AccessDeniedException("You can only view recommendations for classes you are enrolled in");
            }
            return recommendationRepository.findByClassEntityIdAndRecipientId(
                    classId,
                    currentUser.getUserId(),
                    pageable
            ).map(recommendationMapper::toResponse);
        }
        
        return recommendationRepository.findByClassEntityId(classId, pageable)
                .map(recommendationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience, Pageable pageable) {
        log.debug("Fetching recommendations by audience with pagination: audience={}, page={}, size={}", 
                  audience, pageable.getPageNumber(), pageable.getPageSize());
        return recommendationRepository.findByAudience(audience, pageable)
                .map(recommendationMapper::toResponse);
    }

    @Override
    public void deleteRecommendation(UUID id) {
        log.info("Soft deleting recommendation: id={}", id);
        
        AiRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", "id", id));
        
        // ✅ AUTHORIZATION: Verify the current user is the teacher of the class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!recommendation.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Recommendation deletion denied: user {} tried to delete recommendation {} for class owned by teacher {}", 
                     currentUser.getUserId(), id, recommendation.getClassEntity().getTeacher().getId());
            throw new AccessDeniedException("You can only delete recommendations for your classes");
        }
        
        recommendation.softDelete();
        recommendationRepository.save(recommendation);
        log.debug("Recommendation soft deleted successfully: id={}", id);
    }
}

