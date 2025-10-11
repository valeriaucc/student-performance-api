package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.Semester;
import com.viveek.aiclass.dto.request.CreateClassRequest;
import com.viveek.aiclass.dto.request.UpdateClassRequest;
import com.viveek.aiclass.dto.response.ClassResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Class operations.
 */
public interface ClassService {

    ClassResponse createClass(CreateClassRequest request);

    ClassResponse updateClass(UUID id, UpdateClassRequest request);

    ClassResponse getClassById(UUID id);

    List<ClassResponse> getAllClasses();

    List<ClassResponse> getClassesByTeacherId(UUID teacherId);

    List<ClassResponse> getClassesBySubjectId(UUID subjectId);

    List<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester);

    void deleteClass(UUID id);
}

