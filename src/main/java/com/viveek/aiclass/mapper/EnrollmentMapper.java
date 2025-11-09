package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.Enrollment;
import com.viveek.aiclass.dto.response.EnrollmentResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for Enrollment entity conversions.
 * Handles mapping between Enrollment entities and DTOs with custom derived fields.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnrollmentMapper {

    /**
     * Maps Enrollment entity to EnrollmentResponse DTO.
     * Includes custom mapping for className derived from class and subject information.
     *
     * @param enrollment the Enrollment entity
     * @return EnrollmentResponse DTO
     */
    @Mapping(source = "classEntity.id", target = "classId")
    @Mapping(target = "className", expression = "java(buildClassName(enrollment))")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentName")
    @Mapping(source = "student.email", target = "studentEmail")
    EnrollmentResponse toResponse(Enrollment enrollment);

    /**
     * Builds a formatted class name from enrollment's class and subject information.
     *
     * @param enrollment the Enrollment entity
     * @return formatted class name string
     */
    default String buildClassName(Enrollment enrollment) {
        if (enrollment.getClassEntity() != null && enrollment.getClassEntity().getSubject() != null) {
            return enrollment.getClassEntity().getSubject().getName() + " - " + 
                   enrollment.getClassEntity().getGroupCode();
        }
        return null;
    }
}

