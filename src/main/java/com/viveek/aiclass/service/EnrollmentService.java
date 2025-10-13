package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.dto.request.CreateEnrollmentRequest;
import com.viveek.aiclass.dto.request.UpdateEnrollmentRequest;
import com.viveek.aiclass.dto.response.EnrollmentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Enrollment operations.
 */
public interface EnrollmentService {

    EnrollmentResponse enrollStudent(CreateEnrollmentRequest request);

    EnrollmentResponse updateEnrollment(UUID id, UpdateEnrollmentRequest request);

    EnrollmentResponse getEnrollmentById(UUID id);

    List<EnrollmentResponse> getEnrollmentsByClassId(UUID classId);

    List<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId);

    List<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status);

    void deleteEnrollment(UUID id);
}

