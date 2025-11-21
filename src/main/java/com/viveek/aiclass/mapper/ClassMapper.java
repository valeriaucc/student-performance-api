package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.dto.response.ClassResponse;
import org.mapstruct.*;

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
    ClassResponse toResponse(Class classEntity);
}


