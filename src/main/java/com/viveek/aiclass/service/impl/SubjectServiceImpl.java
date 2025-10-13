package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.repository.SubjectRepository;
import com.viveek.aiclass.dto.request.CreateSubjectRequest;
import com.viveek.aiclass.dto.request.UpdateSubjectRequest;
import com.viveek.aiclass.dto.response.SubjectResponse;
import com.viveek.aiclass.exception.ResourceAlreadyExistsException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SubjectService.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        // Validate code uniqueness
        if (subjectRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException("Subject", "code", request.getCode());
        }

        Subject subject = EntityMapper.toSubject(request);
        Subject savedSubject = subjectRepository.save(subject);
        return EntityMapper.toSubjectResponse(savedSubject);
    }

    @Override
    public SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        if (request.getCode() != null) {
            // Check if code is changing and if new code already exists
            if (!request.getCode().equals(subject.getCode()) && 
                subjectRepository.existsByCode(request.getCode())) {
                throw new ResourceAlreadyExistsException("Subject", "code", request.getCode());
            }
            subject.setCode(request.getCode());
        }
        if (request.getName() != null) {
            subject.setName(request.getName());
        }
        if (request.getDescription() != null) {
            subject.setDescription(request.getDescription());
        }

        Subject updatedSubject = subjectRepository.save(subject);
        return EntityMapper.toSubjectResponse(updatedSubject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        return EntityMapper.toSubjectResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectByCode(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "code", code));
        return EntityMapper.toSubjectResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(EntityMapper::toSubjectResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSubject(UUID id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject", "id", id);
        }
        subjectRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return subjectRepository.existsByCode(code);
    }
}

