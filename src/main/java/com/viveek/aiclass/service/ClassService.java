package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.Semester;
import com.viveek.aiclass.dto.request.CreateClassRequest;
import com.viveek.aiclass.dto.request.UpdateClassRequest;
import com.viveek.aiclass.dto.response.ClassResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Class operations.
 * All list operations use pagination for better performance and consistency.
 */
public interface ClassService {

    ClassResponse createClass(CreateClassRequest request);

    ClassResponse updateClass(UUID id, UpdateClassRequest request);

    ClassResponse getClassById(UUID id);

    /**
     * Get all classes with pagination and automatic role-based filtering.
     * Teachers see only their own classes, students see only enrolled classes.
     */
    Page<ClassResponse> getAllClasses(Pageable pageable);

    /**
     * Get classes by teacher ID with pagination.
     */
    Page<ClassResponse> getClassesByTeacherId(UUID teacherId, Pageable pageable);

    /**
     * Get classes by subject ID with pagination.
     */
    Page<ClassResponse> getClassesBySubjectId(UUID subjectId, Pageable pageable);

    /**
     * Get classes by year and semester with pagination.
     */
    Page<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester, Pageable pageable);

    void deleteClass(UUID id);
}

