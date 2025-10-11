package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.enums.Semester;
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

    List<Class> findBySubjectId(UUID subjectId);

    List<Class> findByYearAndSemester(Integer year, Semester semester);

    @Query("SELECT c FROM Class c WHERE c.teacher.id = :teacherId AND c.year = :year AND c.semester = :semester")
    List<Class> findByTeacherAndYearAndSemester(
            @Param("teacherId") UUID teacherId,
            @Param("year") Integer year,
            @Param("semester") Semester semester
    );
}

