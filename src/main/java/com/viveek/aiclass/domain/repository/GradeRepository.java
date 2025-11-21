package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Grade entity operations.
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.classEntity.id = :classId")
    List<Grade> findByClassEntityId(@Param("classId") UUID classId);

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.classEntity.id = :classId")
    Page<Grade> findByClassEntityId(@Param("classId") UUID classId, Pageable pageable);

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.student.id = :studentId")
    List<Grade> findByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.student.id = :studentId")
    Page<Grade> findByStudentId(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.assessmentKind = :assessmentKind")
    List<Grade> findByAssessmentKind(@Param("assessmentKind") String assessmentKind);

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.classEntity.id = :classId AND g.student.id = :studentId")
    List<Grade> findByClassAndStudent(
            @Param("classId") UUID classId,
            @Param("studentId") UUID studentId
    );

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.classEntity.id = :classId AND g.assessmentKind = :assessmentKind")
    List<Grade> findByClassAndAssessmentKind(
            @Param("classId") UUID classId,
            @Param("assessmentKind") String assessmentKind
    );

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.student.id = :studentId AND c.teacher.id = :teacherId")
    Page<Grade> findByStudentIdAndTeacherId(
            @Param("studentId") UUID studentId,
            @Param("teacherId") UUID teacherId,
            Pageable pageable
    );

    /**
     * Find the first grade for a specific assessment in a class.
     * Used to retrieve assessment content that was defined when the assessment was first created.
     * 
     * @param classId the class ID
     * @param assessmentKind the assessment kind (e.g., "EXAM", "QUIZ")
     * @param assessmentName the assessment name (e.g., "Midterm Exam 1")
     * @return the first grade for this assessment, or empty if none exists
     */
    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.classEntity c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH g.student " +
           "WHERE g.classEntity.id = :classId " +
           "AND g.assessmentKind = :assessmentKind " +
           "AND (:assessmentName IS NULL OR g.assessmentName = :assessmentName) " +
           "ORDER BY g.createdAt ASC")
    List<Grade> findByClassAndAssessment(
            @Param("classId") UUID classId,
            @Param("assessmentKind") String assessmentKind,
            @Param("assessmentName") String assessmentName
    );
}

