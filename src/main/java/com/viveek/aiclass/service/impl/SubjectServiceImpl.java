package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.repository.SubjectRepository;
import com.viveek.aiclass.dto.request.CreateSubjectRequest;
import com.viveek.aiclass.dto.request.UpdateSubjectRequest;
import com.viveek.aiclass.dto.response.SubjectResponse;
import com.viveek.aiclass.exception.ResourceAlreadyExistsException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.SubjectMapper;
import com.viveek.aiclass.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SubjectService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        log.info("Creating subject with code={}, name={}", request.getCode(), request.getName());
        
        if (subjectRepository.existsByCode(request.getCode())) {
            log.warn("Subject creation failed: code already exists - {}", request.getCode());
            throw new ResourceAlreadyExistsException("Subject", "code", request.getCode());
        }

        Subject subject = subjectMapper.toEntity(request);
        Subject savedSubject = subjectRepository.save(subject);
        log.debug("Subject created successfully: id={}, code={}", savedSubject.getId(), savedSubject.getCode());
        return subjectMapper.toResponse(savedSubject);
    }

    @Override
    public SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request) {
        log.info("Updating subject: id={}", id);
        
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        if (request.getCode() != null) {
            if (!request.getCode().equals(subject.getCode()) && 
                subjectRepository.existsByCode(request.getCode())) {
                log.warn("Subject update failed: code already exists - {}", request.getCode());
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
        log.debug("Subject updated successfully: id={}", updatedSubject.getId());
        return subjectMapper.toResponse(updatedSubject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        return subjectMapper.toResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectByCode(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "code", code));
        return subjectMapper.toResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        log.debug("Fetching all subjects (non-paginated)");
        return subjectRepository.findAll().stream()
                .map(subjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectResponse> getAllSubjects(Pageable pageable) {
        log.debug("Fetching subjects with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return subjectRepository.findAll(pageable)
                .map(subjectMapper::toResponse);
    }

    @Override
    public void deleteSubject(UUID id) {
        log.info("Soft deleting subject: id={}", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subject deletion failed: subject not found - id={}", id);
                    return new ResourceNotFoundException("Subject", "id", id);
                });
        // Use repository.delete() to trigger @SQLDelete annotation
        subjectRepository.delete(subject);
        log.debug("Subject soft deleted successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return subjectRepository.existsByCode(code);
    }
}

