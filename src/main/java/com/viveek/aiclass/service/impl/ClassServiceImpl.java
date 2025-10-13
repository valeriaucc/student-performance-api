package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.constants.ValidationMessages;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.Semester;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.SubjectRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateClassRequest;
import com.viveek.aiclass.dto.request.UpdateClassRequest;
import com.viveek.aiclass.dto.response.ClassResponse;
import com.viveek.aiclass.exception.BusinessException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.ClassService;
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
 * Implementation of ClassService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @Override
    public ClassResponse createClass(CreateClassRequest request) {
        log.info("Creating class with subject={}, teacher={}, year={}, semester={}", 
                 request.getSubjectId(), request.getTeacherId(), request.getYear(), request.getSemester());
        
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

        if (teacher.getRole() != UserRole.TEACHER) {
            log.warn("Class creation failed: user is not a teacher - userId={}, role={}", teacher.getId(), teacher.getRole());
            throw new BusinessException(ValidationMessages.INVALID_ROLE + ": User must be a TEACHER to teach a class");
        }

        Class classEntity = Class.builder()
                .subject(subject)
                .teacher(teacher)
                .year(request.getYear())
                .semester(request.getSemester())
                .groupCode(request.getGroupCode())
                .metadata(request.getMetadata())
                .build();

        Class savedClass = classRepository.save(classEntity);
        log.debug("Class created successfully: id={}, groupCode={}", savedClass.getId(), savedClass.getGroupCode());
        return EntityMapper.toClassResponse(savedClass);
    }

    @Override
    public ClassResponse updateClass(UUID id, UpdateClassRequest request) {
        log.info("Updating class: id={}", id);
        
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));
            classEntity.setSubject(subject);
        }
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));
            
            if (teacher.getRole() != UserRole.TEACHER) {
                log.warn("Class update failed: user is not a teacher - userId={}, role={}", teacher.getId(), teacher.getRole());
                throw new BusinessException(ValidationMessages.INVALID_ROLE + ": User must be a TEACHER to teach a class");
            }
            
            classEntity.setTeacher(teacher);
        }
        if (request.getYear() != null) {
            classEntity.setYear(request.getYear());
        }
        if (request.getSemester() != null) {
            classEntity.setSemester(request.getSemester());
        }
        if (request.getGroupCode() != null) {
            classEntity.setGroupCode(request.getGroupCode());
        }
        if (request.getMetadata() != null) {
            classEntity.setMetadata(request.getMetadata());
        }

        Class updatedClass = classRepository.save(classEntity);
        log.debug("Class updated successfully: id={}", updatedClass.getId());
        return EntityMapper.toClassResponse(updatedClass);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse getClassById(UUID id) {
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
        return EntityMapper.toClassResponse(classEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getAllClasses() {
        log.debug("Fetching all classes (non-paginated)");
        return classRepository.findAll().stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getAllClasses(Pageable pageable) {
        log.debug("Fetching classes with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return classRepository.findAll(pageable)
                .map(EntityMapper::toClassResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getClassesByTeacherId(UUID teacherId) {
        log.debug("Fetching classes by teacher: teacherId={} (non-paginated)", teacherId);
        return classRepository.findByTeacherId(teacherId).stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getClassesByTeacherId(UUID teacherId, Pageable pageable) {
        log.debug("Fetching classes by teacher with pagination: teacherId={}, page={}, size={}", 
                  teacherId, pageable.getPageNumber(), pageable.getPageSize());
        return classRepository.findByTeacherId(teacherId, pageable)
                .map(EntityMapper::toClassResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getClassesBySubjectId(UUID subjectId) {
        log.debug("Fetching classes by subject: subjectId={} (non-paginated)", subjectId);
        return classRepository.findBySubjectId(subjectId).stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getClassesBySubjectId(UUID subjectId, Pageable pageable) {
        log.debug("Fetching classes by subject with pagination: subjectId={}, page={}, size={}", 
                  subjectId, pageable.getPageNumber(), pageable.getPageSize());
        return classRepository.findBySubjectId(subjectId, pageable)
                .map(EntityMapper::toClassResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester) {
        log.debug("Fetching classes by year and semester: year={}, semester={} (non-paginated)", year, semester);
        return classRepository.findByYearAndSemester(year, semester).stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester, Pageable pageable) {
        log.debug("Fetching classes by year and semester with pagination: year={}, semester={}, page={}, size={}", 
                  year, semester, pageable.getPageNumber(), pageable.getPageSize());
        return classRepository.findByYearAndSemester(year, semester, pageable)
                .map(EntityMapper::toClassResponse);
    }

    @Override
    public void deleteClass(UUID id) {
        log.info("Deleting class: id={}", id);
        if (!classRepository.existsById(id)) {
            log.warn("Class deletion failed: class not found - id={}", id);
            throw new ResourceNotFoundException("Class", "id", id);
        }
        classRepository.deleteById(id);
        log.debug("Class deleted successfully: id={}", id);
    }
}

