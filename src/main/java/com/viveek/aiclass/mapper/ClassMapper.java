package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.dto.response.ClassResponse;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Class entity conversions.
 * Handles mapping between Class entities and DTOs with custom field mapping for related entities.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassMapper {

    /**
     * Maps Class entity to ClassResponse DTO.
     * Custom mappings extract nested field values from related entities.
     *
     * @param classEntity the Class entity
     * @return ClassResponse DTO
     */
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    @Mapping(source = "subject.code", target = "subjectCode")
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher.fullName", target = "teacherName")
    @Mapping(target = "teacherRecommendation", ignore = true)
    @Mapping(target = "students", expression = "java(extractStudents(classEntity))")
    ClassResponse toResponse(Class classEntity);

    /**
     * Extracts student information from enrollments with ACTIVE status.
     * Only includes students with active enrollments.
     *
     * @param classEntity the Class entity
     * @return list of student information
     */
    default List<ClassResponse.StudentInfo> extractStudents(Class classEntity) {
        if (classEntity.getEnrollments() == null) {
            return null;
        }
        
        return classEntity.getEnrollments().stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .filter(enrollment -> enrollment.getStudent() != null)
                .map(enrollment -> {
                    ClassResponse.StudentInfo studentInfo = new ClassResponse.StudentInfo();
                    studentInfo.setId(enrollment.getStudent().getId());
                    studentInfo.setName(enrollment.getStudent().getFullName());
                    studentInfo.setEmail(enrollment.getStudent().getEmail());
                    return studentInfo;
                })
                .collect(Collectors.toList());
    }
}


