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
import com.viveek.aiclass.mapper.EnrollmentMapper;
import com.viveek.aiclass.security.AuthenticatedUser;
import com.viveek.aiclass.security.SecurityContextHelper;
import com.viveek.aiclass.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public EnrollmentResponse enrollStudent(CreateEnrollmentRequest request) {
        log.info("Enrolling student in class: studentId={}, classId={}", 
                 request.getStudentId(), request.getClassId());
        
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        // ✅ AUTHORIZATION: Verify the current user is the teacher of this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Enrollment creation denied: user {} is not the teacher of class {}", 
                     currentUser.getUserId(), request.getClassId());
            throw new AccessDeniedException("You can only enroll students in your own classes");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        if (student.getRole() != UserRole.STUDENT) {
            log.warn("Enrollment failed: user is not a student - userId={}, role={}", student.getId(), student.getRole());
            throw new BusinessException(ValidationMessages.INVALID_ROLE + ": User must be a STUDENT to enroll in a class");
        }

        // Check for existing active enrollment
        Optional<Enrollment> existingActiveEnrollment = enrollmentRepository
                .findByClassAndStudent(request.getClassId(), request.getStudentId());
        
        if (existingActiveEnrollment.isPresent()) {
            log.warn("Enrollment failed: student already actively enrolled - studentId={}, classId={}, existingEnrollmentId={}", 
                     request.getStudentId(), request.getClassId(), existingActiveEnrollment.get().getId());
            throw new BusinessException(ValidationMessages.DUPLICATE_ENROLLMENT);
        }

        // Check for soft-deleted enrollment (to prevent unique constraint violation)
        Optional<Enrollment> existingDeletedEnrollment = enrollmentRepository
                .findByClassAndStudentIncludingDeleted(request.getClassId(), request.getStudentId());
        
        Enrollment enrollment;
        if (existingDeletedEnrollment.isPresent() && existingDeletedEnrollment.get().getDeletedAt() != null) {
            // Restore soft-deleted enrollment instead of creating a new one
            enrollment = existingDeletedEnrollment.get();
            enrollment.setDeletedAt(null); // Un-delete the record
            enrollment.setStatus(request.getStatus() != null ? request.getStatus() : EnrollmentStatus.ACTIVE);
            enrollment.setEnrolledAt(java.time.ZonedDateTime.now()); // Update enrollment time
            log.info("Restoring soft-deleted enrollment: enrollmentId={}, studentId={}, classId={}", 
                     enrollment.getId(), request.getStudentId(), request.getClassId());
        } else {
            // Create new enrollment
            enrollment = Enrollment.builder()
                    .classEntity(classEntity)
                    .student(student)
                    .status(request.getStatus() != null ? request.getStatus() : EnrollmentStatus.ACTIVE)
                    .build();
            log.info("Creating new enrollment: studentId={}, classId={}", 
                     request.getStudentId(), request.getClassId());
        }

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.debug("Student enrolled successfully: enrollmentId={}, status={}", 
                  savedEnrollment.getId(), savedEnrollment.getStatus());
        return enrollmentMapper.toResponse(savedEnrollment);
    }

    @Override
    public EnrollmentResponse updateEnrollment(UUID id, UpdateEnrollmentRequest request) {
        log.info("Updating enrollment: id={}, newStatus={}", id, request.getStatus());
        
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        // ✅ AUTHORIZATION: Verify the current user is the teacher of the class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!enrollment.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Enrollment update denied: user {} tried to update enrollment {} for class owned by teacher {}", 
                     currentUser.getUserId(), id, enrollment.getClassEntity().getTeacher().getId());
            throw new AccessDeniedException("You can only update enrollments for your classes");
        }

        enrollment.setStatus(request.getStatus());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.debug("Enrollment updated successfully: id={}, status={}", 
                  updatedEnrollment.getId(), updatedEnrollment.getStatus());
        return enrollmentMapper.toResponse(updatedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(UUID id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        
        // ✅ AUTHORIZATION: Verify access based on role
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isTeacher()) {
            // Teachers can only view enrollments for their own classes
            if (!enrollment.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Enrollment access denied: teacher {} tried to access enrollment {} for class owned by teacher {}", 
                         currentUser.getUserId(), id, enrollment.getClassEntity().getTeacher().getId());
                throw new AccessDeniedException("You can only view enrollments for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can only view their own enrollments
            if (!enrollment.getStudent().getId().equals(currentUser.getUserId())) {
                log.warn("Enrollment access denied: student {} tried to access enrollment {} for student {}", 
                         currentUser.getUserId(), id, enrollment.getStudent().getId());
                throw new AccessDeniedException("You can only view your own enrollments");
            }
        }
        
        return enrollmentMapper.toResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getEnrollmentsByClassId(UUID classId, Pageable pageable) {
        log.debug("Fetching enrollments by class with pagination: classId={}, page={}, size={}", 
                  classId, pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Verify the teacher owns this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));
        
        if (currentUser.isTeacher()) {
            if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Enrollments access denied: teacher {} tried to access enrollments for class owned by teacher {}", 
                         currentUser.getUserId(), classEntity.getTeacher().getId());
                throw new AccessDeniedException("You can only view enrollments for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students cannot list all enrollments for a class
            throw new AccessDeniedException("Students can only view their own enrollments");
        }
        
        return enrollmentRepository.findByClassEntityId(classId, pageable)
                .map(enrollmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId, Pageable pageable) {
        log.debug("Fetching enrollments by student with pagination: studentId={}, page={}, size={}", 
                  studentId, pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Verify access rights
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isStudent()) {
            // Students can only view their own enrollments
            if (!studentId.equals(currentUser.getUserId())) {
                log.warn("Enrollments access denied: student {} tried to access enrollments for student {}", 
                         currentUser.getUserId(), studentId);
                throw new AccessDeniedException("You can only view your own enrollments");
            }
        } else if (currentUser.isTeacher()) {
            return enrollmentRepository.findByStudentIdAndTeacherId(
                    studentId, 
                    currentUser.getUserId(), 
                    pageable
            ).map(enrollmentMapper::toResponse);
        }
        
        return enrollmentRepository.findByStudentId(studentId, pageable)
                .map(enrollmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status, Pageable pageable) {
        log.debug("Fetching enrollments by status with pagination: status={}, page={}, size={}", 
                  status, pageable.getPageNumber(), pageable.getPageSize());
        return enrollmentRepository.findByStatus(status, pageable)
                .map(enrollmentMapper::toResponse);
    }

    @Override
    public void deleteEnrollment(UUID id) {
        log.info("Soft deleting enrollment: id={}", id);
        
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        
        // ✅ AUTHORIZATION: Verify the current user is the teacher of the class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!enrollment.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Enrollment deletion denied: user {} tried to delete enrollment {} for class owned by teacher {}", 
                     currentUser.getUserId(), id, enrollment.getClassEntity().getTeacher().getId());
            throw new AccessDeniedException("You can only delete enrollments for your classes");
        }
        
        // Use repository.delete() to trigger @SQLDelete annotation
        enrollmentRepository.delete(enrollment);
        log.debug("Enrollment soft deleted successfully: id={}", id);
    }
}

