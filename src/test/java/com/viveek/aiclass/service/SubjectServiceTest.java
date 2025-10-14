package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.repository.SubjectRepository;
import com.viveek.aiclass.dto.request.CreateSubjectRequest;
import com.viveek.aiclass.dto.request.UpdateSubjectRequest;
import com.viveek.aiclass.dto.response.SubjectResponse;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.SubjectMapper;
import com.viveek.aiclass.service.impl.SubjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private Subject testSubject;
    private SubjectResponse testSubjectResponse;
    private UUID subjectId;

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        testSubject = Subject.builder()
                .name("Mathematics")
                .code("MATH101")
                .description("Advanced Mathematics")
                .build();
        ReflectionTestUtils.setField(testSubject, "id", subjectId);
        
        testSubjectResponse = SubjectResponse.builder()
                .id(subjectId)
                .name(testSubject.getName())
                .code(testSubject.getCode())
                .description(testSubject.getDescription())
                .build();
    }

    @Test
    void createSubject_WhenValidRequest_ShouldCreateSubject() {
        CreateSubjectRequest request = CreateSubjectRequest.builder()
                .name("Physics")
                .code("PHYS101")
                .description("Introduction to Physics")
                .build();

        Subject savedSubject = Subject.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .build();
        ReflectionTestUtils.setField(savedSubject, "id", UUID.randomUUID());
        
        SubjectResponse expectedResponse = SubjectResponse.builder()
                .id(savedSubject.getId())
                .name(savedSubject.getName())
                .code(savedSubject.getCode())
                .description(savedSubject.getDescription())
                .build();

        when(subjectMapper.toEntity(request)).thenReturn(savedSubject);
        when(subjectRepository.save(any(Subject.class))).thenReturn(savedSubject);
        when(subjectMapper.toResponse(savedSubject)).thenReturn(expectedResponse);

        SubjectResponse response = subjectService.createSubject(request);

        assertNotNull(response);
        assertEquals(savedSubject.getId(), response.getId());
        verify(subjectMapper).toEntity(request);
        verify(subjectRepository).save(any(Subject.class));
        verify(subjectMapper).toResponse(savedSubject);
    }

    @Test
    void updateSubject_WhenValidRequest_ShouldUpdateSubject() {
        UpdateSubjectRequest request = UpdateSubjectRequest.builder()
                .name("Advanced Mathematics")
                .build();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        when(subjectMapper.toResponse(testSubject)).thenReturn(testSubjectResponse);

        SubjectResponse response = subjectService.updateSubject(subjectId, request);

        assertNotNull(response);
        verify(subjectRepository).save(testSubject);
        verify(subjectMapper).toResponse(testSubject);
    }

    @Test
    void getSubjectById_WhenExists_ShouldReturnSubject() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(subjectMapper.toResponse(testSubject)).thenReturn(testSubjectResponse);

        SubjectResponse response = subjectService.getSubjectById(subjectId);

        assertNotNull(response);
        assertEquals(subjectId, response.getId());
        verify(subjectMapper).toResponse(testSubject);
    }

    @Test
    void getSubjectById_WhenNotFound_ShouldThrowException() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> subjectService.getSubjectById(subjectId));
    }

    @Test
    void getSubjectByCode_WhenExists_ShouldReturnSubject() {
        String code = "MATH101";
        when(subjectRepository.findByCode(code)).thenReturn(Optional.of(testSubject));
        when(subjectMapper.toResponse(testSubject)).thenReturn(testSubjectResponse);

        SubjectResponse response = subjectService.getSubjectByCode(code);

        assertNotNull(response);
        assertEquals(code, response.getCode());
        verify(subjectMapper).toResponse(testSubject);
    }

    @Test
    void getAllSubjects_WithPagination_ShouldReturnPagedSubjects() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Subject> subjectPage = new PageImpl<>(List.of(testSubject), pageable, 1);
        
        when(subjectRepository.findAll(pageable)).thenReturn(subjectPage);
        when(subjectMapper.toResponse(any(Subject.class))).thenReturn(testSubjectResponse);

        Page<SubjectResponse> response = subjectService.getAllSubjects(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(subjectMapper).toResponse(any(Subject.class));
    }

    @Test
    void deleteSubject_WhenExists_ShouldDeleteSubject() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);

        subjectService.deleteSubject(subjectId);

        verify(subjectRepository).save(any(Subject.class));
        assertNotNull(testSubject.getDeletedAt());
    }

    @Test
    void deleteSubject_WhenNotFound_ShouldThrowException() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> subjectService.deleteSubject(subjectId));
        verify(subjectRepository, never()).save(any());
    }
}
