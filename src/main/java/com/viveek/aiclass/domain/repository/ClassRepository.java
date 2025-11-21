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

    @Query("SELECT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.teacher.id = :teacherId")
    List<Class> findByTeacherId(@Param("teacherId") UUID teacherId);

    @Query("SELECT DISTINCT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.enrollments e " +
           "LEFT JOIN FETCH e.student " +
           "WHERE c.teacher.id = :teacherId AND (e.status = 'ACTIVE' OR e IS NULL)")
    Page<Class> findByTeacherId(@Param("teacherId") UUID teacherId, Pageable pageable);

    @Query("SELECT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.subject.id = :subjectId")
    List<Class> findBySubjectId(@Param("subjectId") UUID subjectId);

    @Query("SELECT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.subject.id = :subjectId")
    Page<Class> findBySubjectId(@Param("subjectId") UUID subjectId, Pageable pageable);

    @Query("SELECT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.year = :year AND c.semester = :semester")
    List<Class> findByYearAndSemester(@Param("year") Integer year, @Param("semester") Semester semester);

    @Query("SELECT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.year = :year AND c.semester = :semester")
    Page<Class> findByYearAndSemester(@Param("year") Integer year, @Param("semester") Semester semester, Pageable pageable);

    @Query("SELECT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.teacher.id = :teacherId AND c.year = :year AND c.semester = :semester")
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
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "JOIN Enrollment e ON e.classEntity.id = c.id " +
           "WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    List<Class> findClassesByStudentId(@Param("studentId") UUID studentId);

    /**
     * Find classes where a student is enrolled (with ACTIVE status) - paginated version.
     */
    @Query("SELECT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "JOIN Enrollment e ON e.classEntity.id = c.id " +
           "WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    Page<Class> findClassesByStudentId(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Class c " +
           "WHERE c.id = :classId " +
           "AND c.teacher.id = :teacherId")
    boolean isOwnedByTeacher(
            @Param("classId") UUID classId,
            @Param("teacherId") UUID teacherId
    );

    /**
     * Find class by ID with teacher, subject, and enrollments loaded (for response mapping).
     * Uses fetch join to eagerly load related entities including students.
     */
    @Query("SELECT DISTINCT c FROM Class c " +
           "LEFT JOIN FETCH c.subject " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.enrollments e " +
           "LEFT JOIN FETCH e.student " +
           "WHERE c.id = :id")
    java.util.Optional<Class> findByIdWithRelations(@Param("id") UUID id);
}

