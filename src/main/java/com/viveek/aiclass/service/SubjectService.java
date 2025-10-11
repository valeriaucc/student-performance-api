package com.viveek.aiclass.service;

import com.viveek.aiclass.dto.request.CreateSubjectRequest;
import com.viveek.aiclass.dto.request.UpdateSubjectRequest;
import com.viveek.aiclass.dto.response.SubjectResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Subject operations.
 */
public interface SubjectService {

    SubjectResponse createSubject(CreateSubjectRequest request);

    SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request);

    SubjectResponse getSubjectById(UUID id);

    SubjectResponse getSubjectByCode(String code);

    List<SubjectResponse> getAllSubjects();

    void deleteSubject(UUID id);

    boolean existsByCode(String code);
}

