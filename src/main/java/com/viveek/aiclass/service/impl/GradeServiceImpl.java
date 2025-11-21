package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.constants.ValidationMessages;
import com.viveek.aiclass.domain.model.AiRecommendation;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.Grade;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.domain.repository.AiRecommendationRepository;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.domain.repository.GradeRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.GradeResponse;
import com.viveek.aiclass.dto.response.RecommendationSummary;
import com.viveek.aiclass.exception.BusinessException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.GradeMapper;
import com.viveek.aiclass.security.AuthenticatedUser;
import com.viveek.aiclass.security.SecurityContextHelper;
import com.viveek.aiclass.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of GradeService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AiRecommendationRepository recommendationRepository;
    private final GradeMapper gradeMapper;

    @Override
    public GradeResponse createGrade(CreateGradeRequest request) {
        log.info("Creating grade for student={} in class={}, assessmentKind={}, score={}/{}", 
                 request.getStudentId(), request.getClassId(), request.getAssessmentKind(), 
                 request.getScore(), request.getMaxScore());
        
        if (request.getScore().compareTo(request.getMaxScore()) > 0) {
            log.warn("Grade creation failed: score exceeds max score - score={}, maxScore={}", 
                     request.getScore(), request.getMaxScore());
            throw new BusinessException(ValidationMessages.SCORE_EXCEEDS_MAX);
        }

        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        // ✅ AUTHORIZATION: Verify the current user is the teacher of this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Grade creation denied: user {} is not the teacher of class {}", 
                     currentUser.getUserId(), request.getClassId());
            throw new AccessDeniedException("You can only create grades for students in your classes");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        Enrollment enrollment = enrollmentRepository
                .findByClassAndStudent(request.getClassId(), request.getStudentId())
                .orElseThrow(() -> {
                    log.warn("Grade creation failed: student not enrolled - studentId={}, classId={}", 
                             request.getStudentId(), request.getClassId());
                    return new BusinessException(ValidationMessages.STUDENT_NOT_ENROLLED);
                });
        
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            log.warn("Grade creation failed: enrollment not active - enrollmentId={}, status={}", 
                     enrollment.getId(), enrollment.getStatus());
            throw new BusinessException(ValidationMessages.ENROLLMENT_NOT_ACTIVE);
        }

        Grade grade = Grade.builder()
                .classEntity(classEntity)
                .student(student)
                .assessmentKind(request.getAssessmentKind())
                .assessmentName(request.getAssessmentName())
                .score(request.getScore())
                .maxScore(request.getMaxScore())
                .gradedAt(request.getGradedAt() != null ? request.getGradedAt() : ZonedDateTime.now())
                .metadata(request.getMetadata())
                .build();

        Grade savedGrade = gradeRepository.save(grade);
        log.debug("Grade created successfully: id={}", savedGrade.getId());
        return gradeMapper.toResponse(savedGrade);
    }

    @Override
    public GradeResponse updateGrade(UUID id, UpdateGradeRequest request) {
        log.info("Updating grade: id={}", id);
        
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));

        // ✅ AUTHORIZATION: Verify the current user is the teacher of the class this grade belongs to
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!grade.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Grade update denied: user {} tried to update grade {} for class owned by teacher {}", 
                     currentUser.getUserId(), id, grade.getClassEntity().getTeacher().getId());
            throw new AccessDeniedException("You can only update grades for students in your classes");
        }

        if (request.getAssessmentKind() != null) {
            grade.setAssessmentKind(request.getAssessmentKind());
        }
        if (request.getAssessmentName() != null) {
            grade.setAssessmentName(request.getAssessmentName());
        }
        if (request.getScore() != null) {
            grade.setScore(request.getScore());
        }
        if (request.getMaxScore() != null) {
            grade.setMaxScore(request.getMaxScore());
        }
        
        if (grade.getScore().compareTo(grade.getMaxScore()) > 0) {
            log.warn("Grade update failed: score exceeds max score - score={}, maxScore={}", 
                     grade.getScore(), grade.getMaxScore());
            throw new BusinessException(ValidationMessages.SCORE_EXCEEDS_MAX);
        }
        
        if (request.getGradedAt() != null) {
            grade.setGradedAt(request.getGradedAt());
        }
        if (request.getMetadata() != null) {
            grade.setMetadata(request.getMetadata());
        }

        Grade updatedGrade = gradeRepository.save(grade);
        log.debug("Grade updated successfully: id={}", updatedGrade.getId());
        return gradeMapper.toResponse(updatedGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getGradeById(UUID id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        
        // ✅ AUTHORIZATION: Verify access based on role
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isTeacher()) {
            // Teachers can only view grades for their own classes
            if (!grade.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Grade access denied: teacher {} tried to access grade {} for class owned by teacher {}", 
                         currentUser.getUserId(), id, grade.getClassEntity().getTeacher().getId());
                throw new AccessDeniedException("You can only view grades for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can only view their own grades
            if (!grade.getStudent().getId().equals(currentUser.getUserId())) {
                log.warn("Grade access denied: student {} tried to access grade {} for student {}", 
                         currentUser.getUserId(), id, grade.getStudent().getId());
                throw new AccessDeniedException("You can only view your own grades");
            }
        }
        
        GradeResponse response = gradeMapper.toResponse(grade);
        enrichWithRecommendation(response, grade.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeResponse> getGradesByClassId(UUID classId, Pageable pageable) {
        log.debug("Fetching grades by class with pagination: classId={}, page={}, size={}", 
                  classId, pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Verify the teacher owns this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));
        
        if (currentUser.isTeacher()) {
            if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Grades access denied: teacher {} tried to access grades for class owned by teacher {}", 
                         currentUser.getUserId(), classEntity.getTeacher().getId());
                throw new AccessDeniedException("You can only view grades for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students cannot list all grades for a class, only their own
            throw new AccessDeniedException("Students can only view their own grades");
        }
        
        Page<GradeResponse> gradePage = gradeRepository.findByClassEntityId(classId, pageable)
                .map(gradeMapper::toResponse);
        
        // Enrich with recommendations in batch
        enrichGradesWithRecommendations(gradePage.getContent());
        
        return gradePage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeResponse> getGradesByStudentId(UUID studentId, Pageable pageable) {
        log.debug("Fetching grades by student with pagination: studentId={}, page={}, size={}", 
                  studentId, pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Verify access rights
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isStudent()) {
            // Students can only view their own grades
            if (!studentId.equals(currentUser.getUserId())) {
                log.warn("Grades access denied: student {} tried to access grades for student {}", 
                         currentUser.getUserId(), studentId);
                throw new AccessDeniedException("You can only view your own grades");
            }
        } else if (currentUser.isTeacher()) {
            Page<GradeResponse> gradePage = gradeRepository.findByStudentIdAndTeacherId(
                    studentId,
                    currentUser.getUserId(),
                    pageable
            ).map(gradeMapper::toResponse);
            
            // Enrich with recommendations in batch
            enrichGradesWithRecommendations(gradePage.getContent());
            
            return gradePage;
        }
        
        Page<GradeResponse> gradePage = gradeRepository.findByStudentId(studentId, pageable)
                .map(gradeMapper::toResponse);
        
        // Enrich with recommendations in batch
        enrichGradesWithRecommendations(gradePage.getContent());
        
        return gradePage;
    }

    /**
     * Enriches a single grade response with its recommendation (if exists).
     */
    private void enrichWithRecommendation(GradeResponse gradeResponse, UUID gradeId) {
        recommendationRepository.findByGradeId(gradeId)
                .ifPresent(recommendation -> {
                    RecommendationSummary summary = RecommendationSummary.builder()
                            .id(recommendation.getId())
                            .message(recommendation.getMessage())
                            .build();
                    gradeResponse.setRecommendation(summary);
                });
    }

    /**
     * Enriches multiple grade responses with their recommendations in batch (efficient).
     * Fetches all recommendations in one query to avoid N+1 problem.
     */
    private void enrichGradesWithRecommendations(List<GradeResponse> grades) {
        if (grades == null || grades.isEmpty()) {
            return;
        }

        // Collect all grade IDs
        List<UUID> gradeIds = grades.stream()
                .map(GradeResponse::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (gradeIds.isEmpty()) {
            return;
        }

        // Fetch all recommendations in one batch query
        List<AiRecommendation> recommendations = recommendationRepository.findByGradeIds(gradeIds);

        // Create a map for O(1) lookup: gradeId -> recommendation
        Map<UUID, AiRecommendation> recommendationMap = recommendations.stream()
                .filter(r -> r.getGrade() != null && r.getGrade().getId() != null)
                .collect(Collectors.toMap(
                        r -> r.getGrade().getId(),
                        r -> r,
                        (existing, replacement) -> existing // If multiple recommendations exist, keep first
                ));

        // Enrich each grade with its recommendation
        grades.forEach(grade -> {
            AiRecommendation recommendation = recommendationMap.get(grade.getId());
            if (recommendation != null) {
                RecommendationSummary summary = RecommendationSummary.builder()
                        .id(recommendation.getId())
                        .message(recommendation.getMessage())
                        .build();
                grade.setRecommendation(summary);
            }
        });
    }

    @Override
    public void deleteGrade(UUID id) {
        log.info("Soft deleting grade: id={}", id);
        
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        
        // ✅ AUTHORIZATION: Verify the current user is the teacher of the class this grade belongs to
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!grade.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Grade deletion denied: user {} tried to delete grade {} for class owned by teacher {}", 
                     currentUser.getUserId(), id, grade.getClassEntity().getTeacher().getId());
            throw new AccessDeniedException("You can only delete grades for students in your classes");
        }
        
        // Use repository.delete() to trigger @SQLDelete annotation
        gradeRepository.delete(grade);
        log.debug("Grade soft deleted successfully: id={}", id);
    }
}

