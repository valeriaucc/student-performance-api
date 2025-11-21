package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.AiRecommendation;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for AiRecommendation entity conversions.
 * Handles mapping between AiRecommendation entities and DTOs with custom derived fields.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecommendationMapper {

    /**
     * Maps AiRecommendation entity to RecommendationResponse DTO.
     * Includes custom mapping for className derived from class and subject information.
     *
     * @param recommendation the AiRecommendation entity
     * @return RecommendationResponse DTO
     */
    @Mapping(source = "classEntity.id", target = "classId")
    @Mapping(target = "className", expression = "java(buildClassName(recommendation))")
    @Mapping(source = "grade.id", target = "gradeId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "recipient.id", target = "recipientId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "recipientName", expression = "java(getRecipientName(recommendation))")
    RecommendationResponse toResponse(AiRecommendation recommendation);

    /**
     * Builds a formatted class name from recommendation's class and subject information.
     *
     * @param recommendation the AiRecommendation entity
     * @return formatted class name string
     */
    default String buildClassName(AiRecommendation recommendation) {
        if (recommendation == null) {
            return null;
        }
        if (recommendation.getClassEntity() == null) {
            return null;
        }
        if (recommendation.getClassEntity().getSubject() == null) {
            // If subject is null, try to use just the group code
            return recommendation.getClassEntity().getGroupCode() != null 
                ? recommendation.getClassEntity().getGroupCode() 
                : null;
        }
        String subjectName = recommendation.getClassEntity().getSubject().getName();
        String groupCode = recommendation.getClassEntity().getGroupCode();
        if (subjectName == null && groupCode == null) {
            return null;
        }
        if (subjectName == null) {
            return groupCode;
        }
        if (groupCode == null) {
            return subjectName;
        }
        return subjectName + " - " + groupCode;
    }

    /**
     * Safely extracts recipient name from recommendation.
     *
     * @param recommendation the AiRecommendation entity
     * @return recipient name or null
     */
    default String getRecipientName(AiRecommendation recommendation) {
        if (recommendation == null || recommendation.getRecipient() == null) {
            return null;
        }
        return recommendation.getRecipient().getFullName();
    }
}


