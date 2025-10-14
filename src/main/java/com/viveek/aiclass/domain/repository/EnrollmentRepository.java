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

    List<Enrollment> findByClassEntityId(UUID classId);

    Page<Enrollment> findByClassEntityId(UUID classId, Pageable pageable);

    List<Enrollment> findByStudentId(UUID studentId);

    Page<Enrollment> findByStudentId(UUID studentId, Pageable pageable);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    Page<Enrollment> findByStatus(EnrollmentStatus status, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.classEntity.id = :classId AND e.student.id = :studentId")
    Optional<Enrollment> findByClassAndStudent(
            @Param("classId") UUID classId,
            @Param("studentId") UUID studentId
    );

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = :status")
    List<Enrollment> findByStudentAndStatus(
            @Param("studentId") UUID studentId,
            @Param("status") EnrollmentStatus status
    );

    @Query("SELECT e FROM Enrollment e WHERE e.classEntity.id = :classId AND e.status = :status")
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
}

