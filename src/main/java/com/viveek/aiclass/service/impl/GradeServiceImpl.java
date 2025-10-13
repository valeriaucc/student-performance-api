package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Grade;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.GradeRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.GradeResponse;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of GradeService.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @Override
    public GradeResponse createGrade(CreateGradeRequest request) {
        // Validate class exists
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        // Validate student exists
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

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
        return EntityMapper.toGradeResponse(savedGrade);
    }

    @Override
    public GradeResponse updateGrade(UUID id, UpdateGradeRequest request) {
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
        if (request.getGradedAt() != null) {
            grade.setGradedAt(request.getGradedAt());
        }

        Grade updatedGrade = gradeRepository.save(grade);
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
        return gradeRepository.findByClassEntityId(classId).stream()
                .map(EntityMapper::toGradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByStudentId(UUID studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(EntityMapper::toGradeResponse)
                .collect(Collectors.toList());
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
        if (!gradeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Grade", "id", id);
        }
        gradeRepository.deleteById(id);
    }
}

