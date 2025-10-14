package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.constants.ValidationMessages;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.Semester;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.domain.repository.SubjectRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateClassRequest;
import com.viveek.aiclass.dto.request.UpdateClassRequest;
import com.viveek.aiclass.dto.response.ClassResponse;
import com.viveek.aiclass.exception.BusinessException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.ClassMapper;
import com.viveek.aiclass.security.AuthenticatedUser;
import com.viveek.aiclass.security.SecurityContextHelper;
import com.viveek.aiclass.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    private final EnrollmentRepository enrollmentRepository;
    private final ClassMapper classMapper;

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
        return classMapper.toResponse(savedClass);
    }

    @Override
    public ClassResponse updateClass(UUID id, UpdateClassRequest request) {
        log.info("Updating class: id={}", id);
        
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));

        // ✅ AUTHORIZATION: Verify the current user is the teacher of this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Class update denied: user {} is not the teacher of class {}", 
                     currentUser.getUserId(), id);
            throw new AccessDeniedException("You can only update your own classes");
        }

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
        return classMapper.toResponse(updatedClass);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse getClassById(UUID id) {
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
        
        // ✅ AUTHORIZATION: Verify access based on role
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isTeacher()) {
            // Teachers can only view their own classes
            if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Class access denied: teacher {} tried to access class {} owned by teacher {}", 
                         currentUser.getUserId(), id, classEntity.getTeacher().getId());
                throw new AccessDeniedException("You can only access your own classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can only view classes they're enrolled in
            if (!enrollmentRepository.isStudentEnrolledInClass(currentUser.getUserId(), id)) {
                log.warn("Class access denied: student {} tried to access class {} without enrollment", 
                         currentUser.getUserId(), id);
                throw new AccessDeniedException("You can only access classes you are enrolled in");
            }
        }
        
        return classMapper.toResponse(classEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getAllClasses(Pageable pageable) {
        log.debug("Fetching classes with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Auto-filter based on user role
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isTeacher()) {
            // Teachers only see their own classes
            log.debug("Filtering classes for teacher: {} with pagination", currentUser.getUserId());
            return getClassesByTeacherId(currentUser.getUserId(), pageable);
        } else if (currentUser.isStudent()) {
            // Students only see classes they're enrolled in
            log.debug("Filtering classes for student: {} with pagination", currentUser.getUserId());
            return classRepository.findClassesByStudentId(currentUser.getUserId(), pageable)
                    .map(classMapper::toResponse);
        }
        
        throw new AccessDeniedException("Invalid user role for accessing classes");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getClassesByTeacherId(UUID teacherId, Pageable pageable) {
        log.debug("Fetching classes by teacher with pagination: teacherId={}, page={}, size={}", 
                  teacherId, pageable.getPageNumber(), pageable.getPageSize());
        return classRepository.findByTeacherId(teacherId, pageable)
                .map(classMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getClassesBySubjectId(UUID subjectId, Pageable pageable) {
        log.debug("Fetching classes by subject with pagination: subjectId={}, page={}, size={}", 
                  subjectId, pageable.getPageNumber(), pageable.getPageSize());
        return classRepository.findBySubjectId(subjectId, pageable)
                .map(classMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester, Pageable pageable) {
        log.debug("Fetching classes by year and semester with pagination: year={}, semester={}, page={}, size={}", 
                  year, semester, pageable.getPageNumber(), pageable.getPageSize());
        return classRepository.findByYearAndSemester(year, semester, pageable)
                .map(classMapper::toResponse);
    }

    @Override
    public void deleteClass(UUID id) {
        log.info("Deleting class: id={}", id);
        
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
        
        // ✅ AUTHORIZATION: Verify the current user is the teacher of this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Class deletion denied: user {} is not the teacher of class {}", 
                     currentUser.getUserId(), id);
            throw new AccessDeniedException("You can only delete your own classes");
        }
        
        classRepository.deleteById(id);
        log.debug("Class deleted successfully: id={}", id);
    }
}

