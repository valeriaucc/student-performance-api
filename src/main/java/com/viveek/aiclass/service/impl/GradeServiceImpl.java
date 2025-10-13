package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.constants.ValidationMessages;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.Grade;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.domain.repository.GradeRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.GradeResponse;
import com.viveek.aiclass.exception.BusinessException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
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
                .build();

        Grade savedGrade = gradeRepository.save(grade);
        log.debug("Grade created successfully: id={}", savedGrade.getId());
        return EntityMapper.toGradeResponse(savedGrade);
    }

    @Override
    public GradeResponse updateGrade(UUID id, UpdateGradeRequest request) {
        log.info("Updating grade: id={}", id);
        
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));

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

        Grade updatedGrade = gradeRepository.save(grade);
        log.debug("Grade updated successfully: id={}", updatedGrade.getId());
        return EntityMapper.toGradeResponse(updatedGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getGradeById(UUID id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        return EntityMapper.toGradeResponse(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByClassId(UUID classId) {
        log.debug("Fetching grades by class: classId={} (non-paginated)", classId);
        return gradeRepository.findByClassEntityId(classId).stream()
                .map(EntityMapper::toGradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeResponse> getGradesByClassId(UUID classId, Pageable pageable) {
        log.debug("Fetching grades by class with pagination: classId={}, page={}, size={}", 
                  classId, pageable.getPageNumber(), pageable.getPageSize());
        return gradeRepository.findByClassEntityId(classId, pageable)
                .map(EntityMapper::toGradeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByStudentId(UUID studentId) {
        log.debug("Fetching grades by student: studentId={} (non-paginated)", studentId);
        return gradeRepository.findByStudentId(studentId).stream()
                .map(EntityMapper::toGradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeResponse> getGradesByStudentId(UUID studentId, Pageable pageable) {
        log.debug("Fetching grades by student with pagination: studentId={}, page={}, size={}", 
                  studentId, pageable.getPageNumber(), pageable.getPageSize());
        return gradeRepository.findByStudentId(studentId, pageable)
                .map(EntityMapper::toGradeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByClassAndStudent(UUID classId, UUID studentId) {
        return gradeRepository.findByClassAndStudent(classId, studentId).stream()
                .map(EntityMapper::toGradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteGrade(UUID id) {
        log.info("Deleting grade: id={}", id);
        if (!gradeRepository.existsById(id)) {
            log.warn("Grade deletion failed: grade not found - id={}", id);
            throw new ResourceNotFoundException("Grade", "id", id);
        }
        gradeRepository.deleteById(id);
        log.debug("Grade deleted successfully: id={}", id);
    }
}

