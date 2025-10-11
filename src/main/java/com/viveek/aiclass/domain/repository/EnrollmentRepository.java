package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
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

    List<Enrollment> findByStudentId(UUID studentId);

    List<Enrollment> findByStatus(EnrollmentStatus status);

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
}

