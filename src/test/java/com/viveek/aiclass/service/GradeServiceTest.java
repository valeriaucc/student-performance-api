package com.viveek.aiclass.service;

import com.viveek.aiclass.constants.ValidationMessages;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.Grade;
import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.domain.repository.GradeRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.GradeResponse;
import com.viveek.aiclass.exception.BusinessException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.security.AuthenticatedUser;
import com.viveek.aiclass.security.SecurityContextHelper;
import com.viveek.aiclass.service.impl.GradeServiceImpl;
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private MockedStatic<SecurityContextHelper> securityContextHelperMock;
    
    private Class testClass;
    private User testStudent;
    private User testTeacher;
    private Enrollment testEnrollment;
    private Subject testSubject;
    private UUID classId;
    private UUID studentId;
    private UUID teacherId;
    private AuthenticatedUser authenticatedTeacher;
    private AuthenticatedUser authenticatedStudent;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        teacherId = UUID.randomUUID();

        testSubject = Subject.builder()
                .name("Mathematics")
                .code("MATH101")
                .build();
        ReflectionTestUtils.setField(testSubject, "id", UUID.randomUUID());

        // Create test teacher
        testTeacher = User.builder()
                .authUserId(UUID.randomUUID())
                .role(UserRole.TEACHER)
                .fullName("Jane Teacher")
                .email("teacher@example.com")
                .build();
        ReflectionTestUtils.setField(testTeacher, "id", teacherId);

        testClass = Class.builder()
                .subject(testSubject)
                .teacher(testTeacher)
                .groupCode("A1")
                .build();
        ReflectionTestUtils.setField(testClass, "id", classId);

        testStudent = User.builder()
                .authUserId(UUID.randomUUID())
                .role(UserRole.STUDENT)
                .fullName("John Doe")
                .email("john@example.com")
                .build();
        ReflectionTestUtils.setField(testStudent, "id", studentId);

        testEnrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testEnrollment, "id", UUID.randomUUID());

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
    }

    @AfterEach
    void tearDown() {
        if (securityContextHelperMock != null) {
            securityContextHelperMock.close();
        }
    }

    @Test
    void createGrade_WhenValidRequest_ShouldCreateGrade() {
        // Mock authenticated teacher
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        CreateGradeRequest request = CreateGradeRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .assessmentKind("Quiz")
                .assessmentName("Quiz 1")
                .score(new BigDecimal("85.5"))
                .maxScore(new BigDecimal("100.0"))
                .build();

        Grade savedGrade = Grade.builder()
                .classEntity(testClass)
                .student(testStudent)
                .assessmentKind(request.getAssessmentKind())
                .assessmentName(request.getAssessmentName())
                .score(request.getScore())
                .maxScore(request.getMaxScore())
                .gradedAt(ZonedDateTime.now())
                .build();
        ReflectionTestUtils.setField(savedGrade, "id", UUID.randomUUID());

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByClassAndStudent(classId, studentId))
                .thenReturn(Optional.of(testEnrollment));
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        GradeResponse response = gradeService.createGrade(request);

        assertNotNull(response);
        verify(gradeRepository).save(any(Grade.class));
        verify(enrollmentRepository).findByClassAndStudent(classId, studentId);
    }

    @Test
    void createGrade_WhenScoreExceedsMax_ShouldThrowException() {
        CreateGradeRequest request = CreateGradeRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .assessmentKind("Quiz")
                .score(new BigDecimal("150.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.createGrade(request));
        assertTrue(exception.getMessage().contains(ValidationMessages.SCORE_EXCEEDS_MAX));
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void createGrade_WhenClassNotFound_ShouldThrowException() {
        CreateGradeRequest request = CreateGradeRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .assessmentKind("Quiz")
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.createGrade(request));
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void createGrade_WhenStudentNotFound_ShouldThrowException() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        CreateGradeRequest request = CreateGradeRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .assessmentKind("Quiz")
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.createGrade(request));
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void createGrade_WhenStudentNotEnrolled_ShouldThrowException() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        CreateGradeRequest request = CreateGradeRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .assessmentKind("Quiz")
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByClassAndStudent(classId, studentId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.createGrade(request));
        assertTrue(exception.getMessage().contains(ValidationMessages.STUDENT_NOT_ENROLLED));
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void createGrade_WhenEnrollmentNotActive_ShouldThrowException() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        testEnrollment.setStatus(EnrollmentStatus.DROPPED);
        CreateGradeRequest request = CreateGradeRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .assessmentKind("Quiz")
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByClassAndStudent(classId, studentId))
                .thenReturn(Optional.of(testEnrollment));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.createGrade(request));
        assertTrue(exception.getMessage().contains(ValidationMessages.ENROLLMENT_NOT_ACTIVE));
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void updateGrade_WhenValidRequest_ShouldUpdateGrade() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID gradeId = UUID.randomUUID();
        Grade existingGrade = Grade.builder()
                .classEntity(testClass)
                .student(testStudent)
                .assessmentKind("Quiz")
                .score(new BigDecimal("80.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();
        ReflectionTestUtils.setField(existingGrade, "id", gradeId);

        UpdateGradeRequest request = UpdateGradeRequest.builder()
                .score(new BigDecimal("90.0"))
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(existingGrade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(existingGrade);

        GradeResponse response = gradeService.updateGrade(gradeId, request);

        assertNotNull(response);
        verify(gradeRepository).save(existingGrade);
    }

    @Test
    void updateGrade_WhenScoreExceedsMax_ShouldThrowException() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID gradeId = UUID.randomUUID();
        Grade existingGrade = Grade.builder()
                .classEntity(testClass)
                .student(testStudent)
                .score(new BigDecimal("80.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();
        ReflectionTestUtils.setField(existingGrade, "id", gradeId);

        UpdateGradeRequest request = UpdateGradeRequest.builder()
                .score(new BigDecimal("150.0"))
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(existingGrade));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.updateGrade(gradeId, request));
        assertTrue(exception.getMessage().contains(ValidationMessages.SCORE_EXCEEDS_MAX));
    }

    @Test
    void getGradeById_WhenExists_ShouldReturnGrade() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID gradeId = UUID.randomUUID();
        Grade grade = Grade.builder()
                .classEntity(testClass)
                .student(testStudent)
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();
        ReflectionTestUtils.setField(grade, "id", gradeId);

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        GradeResponse response = gradeService.getGradeById(gradeId);

        assertNotNull(response);
        assertEquals(gradeId, response.getId());
    }

    @Test
    void getGradeById_WhenNotFound_ShouldThrowException() {
        UUID gradeId = UUID.randomUUID();
        when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.getGradeById(gradeId));
    }

    @Test
    void getGradesByClassId_WithPagination_ShouldReturnPagedGrades() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        Pageable pageable = PageRequest.of(0, 10);
        Grade grade = Grade.builder()
                .classEntity(testClass)
                .student(testStudent)
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();
        ReflectionTestUtils.setField(grade, "id", UUID.randomUUID());

        Page<Grade> gradePage = new PageImpl<>(List.of(grade), pageable, 1);
        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(gradeRepository.findByClassEntityId(classId, pageable)).thenReturn(gradePage);

        Page<GradeResponse> response = gradeService.getGradesByClassId(classId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }

    @Test
    void getGradesByStudentId_WithPagination_ShouldReturnPagedGrades() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedStudent);
        
        Pageable pageable = PageRequest.of(0, 10);
        Grade grade = Grade.builder()
                .classEntity(testClass)
                .student(testStudent)
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();
        ReflectionTestUtils.setField(grade, "id", UUID.randomUUID());

        Page<Grade> gradePage = new PageImpl<>(List.of(grade), pageable, 1);
        when(gradeRepository.findByStudentId(studentId, pageable)).thenReturn(gradePage);

        Page<GradeResponse> response = gradeService.getGradesByStudentId(studentId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void deleteGrade_WhenExists_ShouldDeleteGrade() {
        securityContextHelperMock.when(SecurityContextHelper::requireAuthentication)
                .thenReturn(authenticatedTeacher);
        
        UUID gradeId = UUID.randomUUID();
        Grade grade = Grade.builder()
                .classEntity(testClass)
                .student(testStudent)
                .score(new BigDecimal("85.0"))
                .maxScore(new BigDecimal("100.0"))
                .build();
        ReflectionTestUtils.setField(grade, "id", gradeId);
        
        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        gradeService.deleteGrade(gradeId);

        verify(gradeRepository).deleteById(gradeId);
    }

    @Test
    void deleteGrade_WhenNotFound_ShouldThrowException() {
        UUID gradeId = UUID.randomUUID();
        when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.deleteGrade(gradeId));
        verify(gradeRepository, never()).deleteById(any());
    }
}
