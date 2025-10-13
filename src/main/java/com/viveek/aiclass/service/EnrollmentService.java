package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.dto.request.CreateEnrollmentRequest;
import com.viveek.aiclass.dto.request.UpdateEnrollmentRequest;
import com.viveek.aiclass.dto.response.EnrollmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Enrollment operations.
 * All list operations use pagination for better performance and consistency.
 */
public interface EnrollmentService {

    EnrollmentResponse enrollStudent(CreateEnrollmentRequest request);

    EnrollmentResponse updateEnrollment(UUID id, UpdateEnrollmentRequest request);

    EnrollmentResponse getEnrollmentById(UUID id);

    /**
     * Get enrollments by class ID with pagination and authorization.
     * Teachers must own the class, students cannot use this endpoint.
     */
    Page<EnrollmentResponse> getEnrollmentsByClassId(UUID classId, Pageable pageable);

    /**
     * Get enrollments by student ID with pagination and authorization.
     * Students can only view their own enrollments, teachers can view enrollments for students in their classes.
     */
    Page<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId, Pageable pageable);

    /**
     * Get enrollments by status with pagination.
     */
    Page<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status, Pageable pageable);

    void deleteEnrollment(UUID id);
}

