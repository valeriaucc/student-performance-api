package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.dto.request.CreateEnrollmentRequest;
import com.viveek.aiclass.dto.request.UpdateEnrollmentRequest;
import com.viveek.aiclass.dto.response.EnrollmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<EnrollmentResponse> getEnrollmentsByClassId(UUID classId, Pageable pageable);

    List<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId);

    Page<EnrollmentResponse> getEnrollmentsByStudentId(UUID studentId, Pageable pageable);

    List<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status);

    Page<EnrollmentResponse> getEnrollmentsByStatus(EnrollmentStatus status, Pageable pageable);

    void deleteEnrollment(UUID id);
}

