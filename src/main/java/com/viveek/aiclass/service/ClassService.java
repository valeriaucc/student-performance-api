package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.Semester;
import com.viveek.aiclass.dto.request.CreateClassRequest;
import com.viveek.aiclass.dto.request.UpdateClassRequest;
import com.viveek.aiclass.dto.response.ClassResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<ClassResponse> getAllClasses(Pageable pageable);

    List<ClassResponse> getClassesByTeacherId(UUID teacherId);

    Page<ClassResponse> getClassesByTeacherId(UUID teacherId, Pageable pageable);

    List<ClassResponse> getClassesBySubjectId(UUID subjectId);

    Page<ClassResponse> getClassesBySubjectId(UUID subjectId, Pageable pageable);

    List<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester);

    Page<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester, Pageable pageable);

    void deleteClass(UUID id);
}

