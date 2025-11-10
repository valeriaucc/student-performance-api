package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.Subject;
import com.viveek.aiclass.dto.request.CreateSubjectRequest;
import com.viveek.aiclass.dto.request.UpdateSubjectRequest;
import com.viveek.aiclass.dto.response.SubjectResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for Subject entity conversions.
 * Handles mapping between Subject entities and DTOs with automatic field mapping.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

    /**
     * Maps Subject entity to SubjectResponse DTO.
     *
     * @param subject the Subject entity
     * @return SubjectResponse DTO
     */
    SubjectResponse toResponse(Subject subject);

    /**
     * Maps CreateSubjectRequest to Subject entity.
     *
     * @param request the CreateSubjectRequest DTO
     * @return Subject entity
     */
    Subject toEntity(CreateSubjectRequest request);

    /**
     * Updates existing Subject entity from UpdateSubjectRequest.
     * Null values in the request are ignored.
     *
     * @param request the UpdateSubjectRequest DTO
     * @param subject the existing Subject entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateSubjectRequest request, @MappingTarget Subject subject);
}


