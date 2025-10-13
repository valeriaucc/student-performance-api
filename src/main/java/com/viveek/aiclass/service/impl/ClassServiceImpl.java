package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.Semester;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.SubjectRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateClassRequest;
import com.viveek.aiclass.dto.request.UpdateClassRequest;
import com.viveek.aiclass.dto.response.ClassResponse;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ClassService.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @Override
    public ClassResponse createClass(CreateClassRequest request) {
        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        // Validate teacher exists
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

        Class classEntity = Class.builder()
                .subject(subject)
                .teacher(teacher)
                .year(request.getYear())
                .semester(request.getSemester())
                .groupCode(request.getGroupCode())
                .metadata(request.getMetadata())
                .build();

        Class savedClass = classRepository.save(classEntity);
        return EntityMapper.toClassResponse(savedClass);
    }

    @Override
    public ClassResponse updateClass(UUID id, UpdateClassRequest request) {
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
        return classRepository.findAll().stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getClassesByTeacherId(UUID teacherId) {
        return classRepository.findByTeacherId(teacherId).stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getClassesBySubjectId(UUID subjectId) {
        return classRepository.findBySubjectId(subjectId).stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> getClassesByYearAndSemester(Integer year, Semester semester) {
        return classRepository.findByYearAndSemester(year, semester).stream()
                .map(EntityMapper::toClassResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteClass(UUID id) {
        if (!classRepository.existsById(id)) {
            throw new ResourceNotFoundException("Class", "id", id);
        }
        classRepository.deleteById(id);
    }
}

