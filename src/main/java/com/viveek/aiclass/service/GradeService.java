package com.viveek.aiclass.service;

import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.GradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Grade operations.
 * All list operations use pagination for better performance and consistency.
 */
public interface GradeService {

    GradeResponse createGrade(CreateGradeRequest request);

    GradeResponse updateGrade(UUID id, UpdateGradeRequest request);

    GradeResponse getGradeById(UUID id);

    /**
     * Get grades by class ID with pagination and authorization.
     * Teachers must own the class, students cannot use this endpoint.
     */
    Page<GradeResponse> getGradesByClassId(UUID classId, Pageable pageable);

    /**
     * Get grades by student ID with pagination and authorization.
     * Students can only view their own grades, teachers can view grades for students in their classes.
     */
    Page<GradeResponse> getGradesByStudentId(UUID studentId, Pageable pageable);

    void deleteGrade(UUID id);
}

