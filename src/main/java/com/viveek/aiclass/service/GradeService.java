package com.viveek.aiclass.service;

import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.GradeResponse;

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

    List<GradeResponse> getGradesByStudentId(UUID studentId);

    List<GradeResponse> getGradesByClassAndStudent(UUID classId, UUID studentId);

    void deleteGrade(UUID id);
}

