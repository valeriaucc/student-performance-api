package com.viveek.aiclass.service;

import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.GradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Grade operations.
 */
public interface GradeService {

    GradeResponse createGrade(CreateGradeRequest request);

    GradeResponse updateGrade(UUID id, UpdateGradeRequest request);

    GradeResponse getGradeById(UUID id);

    List<GradeResponse> getGradesByClassId(UUID classId);

    Page<GradeResponse> getGradesByClassId(UUID classId, Pageable pageable);

    List<GradeResponse> getGradesByStudentId(UUID studentId);

    Page<GradeResponse> getGradesByStudentId(UUID studentId, Pageable pageable);

    List<GradeResponse> getGradesByClassAndStudent(UUID classId, UUID studentId);

    void deleteGrade(UUID id);
}

