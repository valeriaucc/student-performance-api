package com.viveek.aiclass.service;

import com.viveek.aiclass.constants.ValidationMessages;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateEnrollmentRequest;
import com.viveek.aiclass.dto.request.UpdateEnrollmentRequest;
import com.viveek.aiclass.dto.response.EnrollmentResponse;
import com.viveek.aiclass.exception.BusinessException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.service.impl.EnrollmentServiceImpl;
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
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private Class testClass;
    private User testStudent;
    private User testTeacher;
    private UUID classId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        Subject testSubject = Subject.builder()
                .name("Mathematics")
                .code("MATH101")
                .build();
        ReflectionTestUtils.setField(testSubject, "id", UUID.randomUUID());

        testTeacher = User.builder()
                .authUserId(UUID.randomUUID())
                .role(UserRole.TEACHER)
                .fullName("Jane Teacher")
                .email("jane@example.com")
                .build();
        ReflectionTestUtils.setField(testTeacher, "id", UUID.randomUUID());

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
    }

    @Test
    void enrollStudent_WhenValidRequest_ShouldEnrollStudent() {
        CreateEnrollmentRequest request = CreateEnrollmentRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        Enrollment savedEnrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(savedEnrollment, "id", UUID.randomUUID());

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByClassAndStudent(classId, studentId))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);

        EnrollmentResponse response = enrollmentService.enrollStudent(request);

        assertNotNull(response);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_WhenUserIsNotStudent_ShouldThrowException() {
        User teacher = User.builder()
                .authUserId(UUID.randomUUID())
                .role(UserRole.TEACHER)
                .build();
        ReflectionTestUtils.setField(teacher, "id", studentId);

        CreateEnrollmentRequest request = CreateEnrollmentRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(teacher));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> enrollmentService.enrollStudent(request));
        assertTrue(exception.getMessage().contains(ValidationMessages.INVALID_ROLE));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_WhenDuplicateEnrollment_ShouldThrowException() {
        CreateEnrollmentRequest request = CreateEnrollmentRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .build();

        Enrollment existingEnrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(existingEnrollment, "id", UUID.randomUUID());

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByClassAndStudent(classId, studentId))
                .thenReturn(Optional.of(existingEnrollment));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> enrollmentService.enrollStudent(request));
        assertTrue(exception.getMessage().contains(ValidationMessages.DUPLICATE_ENROLLMENT));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_WhenClassNotFound_ShouldThrowException() {
        CreateEnrollmentRequest request = CreateEnrollmentRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.enrollStudent(request));
    }

    @Test
    void enrollStudent_WhenStudentNotFound_ShouldThrowException() {
        CreateEnrollmentRequest request = CreateEnrollmentRequest.builder()
                .classId(classId)
                .studentId(studentId)
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(testClass));
        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.enrollStudent(request));
    }

    @Test
    void updateEnrollment_WhenValid_ShouldUpdateStatus() {
        UUID enrollmentId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(enrollment, "id", enrollmentId);

        UpdateEnrollmentRequest request = UpdateEnrollmentRequest.builder()
                .status(EnrollmentStatus.DROPPED)
                .build();

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.updateEnrollment(enrollmentId, request);

        assertNotNull(response);
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void getEnrollmentById_WhenExists_ShouldReturnEnrollment() {
        UUID enrollmentId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(enrollment, "id", enrollmentId);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        EnrollmentResponse response = enrollmentService.getEnrollmentById(enrollmentId);

        assertNotNull(response);
        assertEquals(enrollmentId, response.getId());
    }

    @Test
    void getEnrollmentsByClassId_WithPagination_ShouldReturnPagedEnrollments() {
        Pageable pageable = PageRequest.of(0, 10);
        Enrollment enrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(enrollment, "id", UUID.randomUUID());

        Page<Enrollment> enrollmentPage = new PageImpl<>(List.of(enrollment), pageable, 1);
        when(enrollmentRepository.findByClassEntityId(classId, pageable)).thenReturn(enrollmentPage);

        Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByClassId(classId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getEnrollmentsByStudentId_WithPagination_ShouldReturnPagedEnrollments() {
        Pageable pageable = PageRequest.of(0, 10);
        Enrollment enrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(enrollment, "id", UUID.randomUUID());

        Page<Enrollment> enrollmentPage = new PageImpl<>(List.of(enrollment), pageable, 1);
        when(enrollmentRepository.findByStudentId(studentId, pageable)).thenReturn(enrollmentPage);

        Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByStudentId(studentId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getEnrollmentsByStatus_WithPagination_ShouldReturnPagedEnrollments() {
        Pageable pageable = PageRequest.of(0, 10);
        Enrollment enrollment = Enrollment.builder()
                .classEntity(testClass)
                .student(testStudent)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(enrollment, "id", UUID.randomUUID());

        Page<Enrollment> enrollmentPage = new PageImpl<>(List.of(enrollment), pageable, 1);
        when(enrollmentRepository.findByStatus(EnrollmentStatus.ACTIVE, pageable))
                .thenReturn(enrollmentPage);

        Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByStatus(
                EnrollmentStatus.ACTIVE, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void deleteEnrollment_WhenExists_ShouldDeleteEnrollment() {
        UUID enrollmentId = UUID.randomUUID();
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(true);

        enrollmentService.deleteEnrollment(enrollmentId);

        verify(enrollmentRepository).deleteById(enrollmentId);
    }

    @Test
    void deleteEnrollment_WhenNotFound_ShouldThrowException() {
        UUID enrollmentId = UUID.randomUUID();
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.deleteEnrollment(enrollmentId));
        verify(enrollmentRepository, never()).deleteById(any());
    }
}
