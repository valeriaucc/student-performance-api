package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.enums.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Class entity operations.
 */
@Repository
public interface ClassRepository extends JpaRepository<Class, UUID> {

    List<Class> findByTeacherId(UUID teacherId);

    Page<Class> findByTeacherId(UUID teacherId, Pageable pageable);

    List<Class> findBySubjectId(UUID subjectId);

    Page<Class> findBySubjectId(UUID subjectId, Pageable pageable);

    List<Class> findByYearAndSemester(Integer year, Semester semester);

    Page<Class> findByYearAndSemester(Integer year, Semester semester, Pageable pageable);

    @Query("SELECT c FROM Class c WHERE c.teacher.id = :teacherId AND c.year = :year AND c.semester = :semester")
    List<Class> findByTeacherAndYearAndSemester(
            @Param("teacherId") UUID teacherId,
            @Param("year") Integer year,
            @Param("semester") Semester semester
    );

    /**
     * Find classes where a student is enrolled (with ACTIVE status).
     * Used for student authorization - students should only see classes they're enrolled in.
     */
    @Query("SELECT c FROM Class c " +
           "JOIN Enrollment e ON e.classEntity.id = c.id " +
           "WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    List<Class> findClassesByStudentId(@Param("studentId") UUID studentId);

    /**
     * Find classes where a student is enrolled (with ACTIVE status) - paginated version.
     */
    @Query("SELECT c FROM Class c " +
           "JOIN Enrollment e ON e.classEntity.id = c.id " +
           "WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    Page<Class> findClassesByStudentId(@Param("studentId") UUID studentId, Pageable pageable);
}

