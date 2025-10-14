package com.viveek.aiclass.service;

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
import com.viveek.aiclass.service.impl.ClassServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
class ClassServiceTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ClassMapper classMapper;

    @InjectMocks
    private ClassServiceImpl classService;

    private MockedStatic<SecurityContextHelper> securityContextHelperMock;
    
    private Subject testSubject;
    private User testTeacher;
    private User testStudent;
    private UUID subjectId;
    private UUID teacherId;
    private UUID studentId;
    private UUID classId;
    private ClassResponse testClassResponse;
    private AuthenticatedUser authenticatedTeacher;
    private AuthenticatedUser authenticatedStudent;

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        testSubject = Subject.builder()
                .name("Mathematics")
                .code("MATH101")
                .build();
        ReflectionTestUtils.setField(testSubject, "id", subjectId);

        testTeacher = User.builder()
                .authUserId(UUID.randomUUID())
                .role(UserRole.TEACHER)
                .fullName("Jane Teacher")
                .email("jane@example.com")
                .build();
        ReflectionTestUtils.setField(testTeacher, "id", teacherId);

        testStudent = User.builder()
                .authUserId(UUID.randomUUID())
                .role(UserRole.STUDENT)
                .fullName("John Student")
                .email("john@example.com")
                .build();
        ReflectionTestUtils.setField(testStudent, "id", studentId);

        // Create authenticated users for tests
        authenticatedTeacher = AuthenticatedUser.builder()
                .userId(teacherId)
                .authUserId(testTeacher.getAuthUserId())
                .email(testTeacher.getEmail())
                .fullName(testTeacher.getFullName())
                .role(UserRole.TEACHER)
                .build();

        authenticatedStudent = AuthenticatedUser.builder()
                .userId(studentId)
                .authUserId(testStudent.getAuthUserId())
                .email(testStudent.getEmail())
                .fullName(testStudent.getFullName())
                .role(UserRole.STUDENT)
                .build();

        // Mock SecurityContextHelper
        securityContextHelperMock = mockStatic(SecurityContextHelper.class);
        
        // Setup test class response
        classId = UUID.randomUUID();
        testClassResponse = ClassResponse.builder()
                .id(classId)
                .subjectId(subjectId)
                .subjectName(testSubject.getName())
                .subjectCode(testSubject.getCode())
                .teacherId(teacherId)
                .teacherName(testTeacher.getFullName())
                .year(2025)
                .semester(Semester.SPRING)
                .groupCode("A1")
                .build();
    }

    @AfterEach
    void tearDown() {
        if (securityContextHelperMock != null) {
            securityContextHelperMock.close();
        }
    }

    @Test
    void createClass_WhenValidRequest_ShouldCreateClass() {
        CreateClassRequest request = CreateClassRequest.builder()
                .subjectId(subjectId)
                .teacherId(teacherId)
                .year(2025)
                .semester(Semester.SPRING)
                .groupCode("A1")
                .build();

        Class savedClass = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .year(2025)
                .semester(Semester.SPRING)
                .groupCode("A1")
                .build();
        ReflectionTestUtils.setField(savedClass, "id", UUID.randomUUID());

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(testTeacher));
        when(classRepository.save(any(Class.class))).thenReturn(savedClass);
        when(classMapper.toResponse(any(Class.class))).thenReturn(testClassResponse);

        ClassResponse response = classService.createClass(request);

        assertNotNull(response);
        verify(classRepository).save(any(Class.class));
        verify(classMapper).toResponse(any(Class.class));
    }

    @Test
    void createClass_WhenUserIsNotTeacher_ShouldThrowException() {
        CreateClassRequest request = CreateClassRequest.builder()
                .subjectId(subjectId)
                .teacherId(teacherId)
                .year(2025)
                .semester(Semester.SPRING)
                .build();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(testStudent));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> classService.createClass(request));
        assertTrue(exception.getMessage().contains(ValidationMessages.INVALID_ROLE));
        verify(classRepository, never()).save(any());
    }

    @Test
    void createClass_WhenSubjectNotFound_ShouldThrowException() {
        CreateClassRequest request = CreateClassRequest.builder()
                .subjectId(subjectId)
                .teacherId(teacherId)
                .build();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> classService.createClass(request));
    }

    @Test
    void createClass_WhenTeacherNotFound_ShouldThrowException() {
        CreateClassRequest request = CreateClassRequest.builder()
                .subjectId(subjectId)
                .teacherId(teacherId)
                .build();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(userRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> classService.createClass(request));
    }

    @Test
    void updateClass_WhenValidRequest_ShouldUpdateClass() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID classId = UUID.randomUUID();
        Class existingClass = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .year(2025)
                .semester(Semester.SPRING)
                .build();
        ReflectionTestUtils.setField(existingClass, "id", classId);

        UpdateClassRequest request = UpdateClassRequest.builder()
                .groupCode("B2")
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(existingClass));
        when(classRepository.save(any(Class.class))).thenReturn(existingClass);
        when(classMapper.toResponse(any(Class.class))).thenReturn(testClassResponse);

        ClassResponse response = classService.updateClass(classId, request);

        assertNotNull(response);
        verify(classRepository).save(existingClass);
        verify(classMapper).toResponse(any(Class.class));
    }

    @Test
    void updateClass_WhenUpdatingTeacherWithNonTeacher_ShouldThrowException() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID classId = UUID.randomUUID();
        Class existingClass = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .build();
        ReflectionTestUtils.setField(existingClass, "id", classId);

        UpdateClassRequest request = UpdateClassRequest.builder()
                .teacherId(testStudent.getId())
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(existingClass));
        when(userRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> classService.updateClass(classId, request));
        assertTrue(exception.getMessage().contains(ValidationMessages.INVALID_ROLE));
    }

    @Test
    void getClassById_WhenExists_ShouldReturnClass() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID localClassId = UUID.randomUUID();
        Class classEntity = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .build();
        ReflectionTestUtils.setField(classEntity, "id", localClassId);
        
        ClassResponse expectedResponse = ClassResponse.builder()
                .id(localClassId)
                .subjectId(subjectId)
                .subjectName(testSubject.getName())
                .subjectCode(testSubject.getCode())
                .teacherId(teacherId)
                .teacherName(testTeacher.getFullName())
                .build();

        when(classRepository.findById(localClassId)).thenReturn(Optional.of(classEntity));
        when(classMapper.toResponse(classEntity)).thenReturn(expectedResponse);

        ClassResponse response = classService.getClassById(localClassId);

        assertNotNull(response);
        assertEquals(localClassId, response.getId());
        verify(classMapper).toResponse(classEntity);
    }

    @Test
    void getAllClasses_WithPagination_ShouldReturnPagedClasses() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        Pageable pageable = PageRequest.of(0, 10);
        Class classEntity = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .build();
        ReflectionTestUtils.setField(classEntity, "id", UUID.randomUUID());

        Page<Class> classPage = new PageImpl<>(List.of(classEntity), pageable, 1);
        when(classRepository.findByTeacherId(teacherId, pageable)).thenReturn(classPage);
        when(classMapper.toResponse(any(Class.class))).thenReturn(testClassResponse);

        Page<ClassResponse> response = classService.getAllClasses(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(classMapper).toResponse(any(Class.class));
    }

    @Test
    void getClassesByTeacherId_WithPagination_ShouldReturnPagedClasses() {
        Pageable pageable = PageRequest.of(0, 10);
        Class classEntity = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .build();
        ReflectionTestUtils.setField(classEntity, "id", UUID.randomUUID());

        Page<Class> classPage = new PageImpl<>(List.of(classEntity), pageable, 1);
        when(classRepository.findByTeacherId(teacherId, pageable)).thenReturn(classPage);
        when(classMapper.toResponse(any(Class.class))).thenReturn(testClassResponse);

        Page<ClassResponse> response = classService.getClassesByTeacherId(teacherId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(classMapper).toResponse(any(Class.class));
    }

    @Test
    void deleteClass_WhenExists_ShouldDeleteClass() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID classId = UUID.randomUUID();
        Class classEntity = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .build();
        ReflectionTestUtils.setField(classEntity, "id", classId);
        
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));

        classService.deleteClass(classId);

        verify(classRepository).deleteById(classId);
    }

    @Test
    void deleteClass_WhenNotFound_ShouldThrowException() {
        UUID classId = UUID.randomUUID();
        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> classService.deleteClass(classId));
        verify(classRepository, never()).deleteById(any());
    }
}
