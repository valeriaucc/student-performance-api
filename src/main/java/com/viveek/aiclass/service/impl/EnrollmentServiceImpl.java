package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.constants.ValidationMessages;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.domain.model.enums.UserRole;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of EnrollmentService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @Override
    public EnrollmentResponse enrollStudent(CreateEnrollmentRequest request) {
        log.info("Enrolling student in class: studentId={}, classId={}", 
                 request.getStudentId(), request.getClassId());
        
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        if (student.getRole() != UserRole.STUDENT) {
            log.warn("Enrollment failed: user is not a student - userId={}, role={}", student.getId(), student.getRole());
            throw new BusinessException(ValidationMessages.INVALID_ROLE + ": User must be a STUDENT to enroll in a class");
        }

        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByClassAndStudent(request.getClassId(), request.getStudentId());
        
        if (existingEnrollment.isPresent()) {
            log.warn("Enrollment failed: student already enrolled - studentId={}, classId={}, existingEnrollmentId={}", 
                     request.getStudentId(), request.getClassId(), existingEnrollment.get().getId());
            throw new BusinessException(ValidationMessages.DUPLICATE_ENROLLMENT);
        }

        Enrollment enrollment = Enrollment.builder()
                .classEntity(classEntity)
                .student(student)
                .status(request.getStatus() != null ? request.getStatus() : EnrollmentStatus.ACTIVE)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.debug("Student enrolled successfully: enrollmentId={}, status={}", 
                  savedEnrollment.getId(), savedEnrollment.getStatus());
        return EntityMapper.toEnrollmentResponse(savedEnrollment);
    }

    @Override
    public EnrollmentResponse updateEnrollment(UUID id, UpdateEnrollmentRequest request) {
        log.info("Updating enrollment: id={}, newStatus={}", id, request.getStatus());
        
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        enrollment.setStatus(request.getStatus());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.debug("Enrollment updated successfully: id={}, status={}", 
                  updatedEnrollment.getId(), updatedEnrollment.getStatus());
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
        log.debug("Fetching enrollments by class: classId={} (non-paginated)", classId);
        return enrollmentRepository.findByClassEntityId(classId).stream()
                .map(EntityMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getEnrollmentsByClassId(UUID classId, Pageable pageable) {
        log.debug("Fetching enrollments by class with pagination: classId={}, page={}, size={}", 
                  classId, pageable.getPageNumber(), pageable.getPageSize());
        return enrollmentRepository.findByClassEntityId(classId, pageable)
                .map(EntityMapper::toEnrollmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId) {
        log.debug("Fetching enrollments by student: studentId={} (non-paginated)", studentId);
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(EntityMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId, Pageable pageable) {
        log.debug("Fetching enrollments by student with pagination: studentId={}, page={}, size={}", 
                  studentId, pageable.getPageNumber(), pageable.getPageSize());
        return enrollmentRepository.findByStudentId(studentId, pageable)
                .map(EntityMapper::toEnrollmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status) {
        log.debug("Fetching enrollments by status: status={} (non-paginated)", status);
        return enrollmentRepository.findByStatus(status).stream()
                .map(EntityMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status, Pageable pageable) {
        log.debug("Fetching enrollments by status with pagination: status={}, page={}, size={}", 
                  status, pageable.getPageNumber(), pageable.getPageSize());
        return enrollmentRepository.findByStatus(status, pageable)
                .map(EntityMapper::toEnrollmentResponse);
    }

    @Override
    public void deleteEnrollment(UUID id) {
        log.info("Deleting enrollment: id={}", id);
        if (!enrollmentRepository.existsById(id)) {
            log.warn("Enrollment deletion failed: enrollment not found - id={}", id);
            throw new ResourceNotFoundException("Enrollment", "id", id);
        }
        enrollmentRepository.deleteById(id);
        log.debug("Enrollment deleted successfully: id={}", id);
    }
}

