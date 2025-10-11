package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateEnrollmentRequest;
import com.viveek.aiclass.dto.request.UpdateEnrollmentRequest;
import com.viveek.aiclass.dto.response.EnrollmentResponse;
import com.viveek.aiclass.exception.BusinessException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of EnrollmentService.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @Override
    public EnrollmentResponse enrollStudent(CreateEnrollmentRequest request) {
        // Validate class exists
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        // Validate student exists
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        // Check if student is already enrolled in this class
        if (enrollmentRepository.findByClassAndStudent(request.getClassId(), request.getStudentId()).isPresent()) {
            throw new BusinessException("Student is already enrolled in this class");
        }

        Enrollment enrollment = Enrollment.builder()
                .classEntity(classEntity)
                .student(student)
                .status(request.getStatus() != null ? request.getStatus() : EnrollmentStatus.ACTIVE)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return EntityMapper.toEnrollmentResponse(savedEnrollment);
    }

    @Override
    public EnrollmentResponse updateEnrollment(UUID id, UpdateEnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        enrollment.setStatus(request.getStatus());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return EntityMapper.toEnrollmentResponse(updatedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(UUID id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        return EntityMapper.toEnrollmentResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByClassId(UUID classId) {
        return enrollmentRepository.findByClassEntityId(classId).stream()
                .map(EntityMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(EntityMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status).stream()
                .map(EntityMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEnrollment(UUID id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enrollment", "id", id);
        }
        enrollmentRepository.deleteById(id);
    }
}

