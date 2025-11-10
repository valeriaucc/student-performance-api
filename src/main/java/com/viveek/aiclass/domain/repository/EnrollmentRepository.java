package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Enrollment entity operations.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.classEntity.id = :classId")
    List<Enrollment> findByClassEntityId(@Param("classId") UUID classId);

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.classEntity.id = :classId")
    Page<Enrollment> findByClassEntityId(@Param("classId") UUID classId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.student.id = :studentId")
    Page<Enrollment> findByStudentId(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.status = :status")
    List<Enrollment> findByStatus(@Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.status = :status")
    Page<Enrollment> findByStatus(@Param("status") EnrollmentStatus status, Pageable pageable);

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.classEntity.id = :classId AND e.student.id = :studentId")
    Optional<Enrollment> findByClassAndStudent(
            @Param("classId") UUID classId,
            @Param("studentId") UUID studentId
    );

    /**
     * Find enrollment by class and student, including soft-deleted records.
     * Uses native SQL to bypass @Where clause filtering.
     * Used to prevent duplicate enrollment attempts when a soft-deleted record exists.
     */
    @Query(value = "SELECT * FROM enrollments e " +
           "WHERE e.class_id = :classId AND e.student_user_id = :studentId",
           nativeQuery = true)
    Optional<Enrollment> findByClassAndStudentIncludingDeleted(
            @Param("classId") UUID classId,
            @Param("studentId") UUID studentId
    );

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.student.id = :studentId AND e.status = :status")
    List<Enrollment> findByStudentAndStatus(
            @Param("studentId") UUID studentId,
            @Param("status") EnrollmentStatus status
    );

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.classEntity.id = :classId AND e.status = :status")
    List<Enrollment> findByClassAndStatus(
            @Param("classId") UUID classId,
            @Param("status") EnrollmentStatus status
    );

    /**
     * Check if a student is enrolled in a specific class with ACTIVE status.
     * Used for authorization checks.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Enrollment e " +
           "WHERE e.student.id = :studentId AND e.classEntity.id = :classId AND e.status = 'ACTIVE'")
    boolean isStudentEnrolledInClass(
            @Param("studentId") UUID studentId,
            @Param("classId") UUID classId
    );

    /**
     * Check if a class belongs to a specific teacher.
     * Used for authorization checks.
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Class c " +
           "WHERE c.id = :classId AND c.teacher.id = :teacherId")
    boolean isClassOwnedByTeacher(
            @Param("classId") UUID classId,
            @Param("teacherId") UUID teacherId
    );

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Enrollment e " +
           "JOIN e.classEntity c " +
           "WHERE e.student.id = :studentId " +
           "AND c.teacher.id = :teacherId")
    boolean isStudentEnrolledWithTeacher(
            @Param("studentId") UUID studentId,
            @Param("teacherId") UUID teacherId
    );

    @Query("SELECT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH e.student " +
           "WHERE e.student.id = :studentId AND c.teacher.id = :teacherId")
    Page<Enrollment> findByStudentIdAndTeacherId(
            @Param("studentId") UUID studentId,
            @Param("teacherId") UUID teacherId,
            Pageable pageable
    );
}

