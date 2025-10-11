package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.Grade;
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

    List<Grade> findByClassEntityId(UUID classId);

    List<Grade> findByStudentId(UUID studentId);

    List<Grade> findByAssessmentKind(String assessmentKind);

    @Query("SELECT g FROM Grade g WHERE g.classEntity.id = :classId AND g.student.id = :studentId")
    List<Grade> findByClassAndStudent(
            @Param("classId") UUID classId,
            @Param("studentId") UUID studentId
    );

    @Query("SELECT g FROM Grade g WHERE g.classEntity.id = :classId AND g.assessmentKind = :assessmentKind")
    List<Grade> findByClassAndAssessmentKind(
            @Param("classId") UUID classId,
            @Param("assessmentKind") String assessmentKind
    );
}

