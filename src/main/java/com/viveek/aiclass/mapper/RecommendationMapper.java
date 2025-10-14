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
    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "recipient.fullName", target = "recipientName")
    RecommendationResponse toResponse(AiRecommendation recommendation);

    /**
     * Builds a formatted class name from recommendation's class and subject information.
     *
     * @param recommendation the AiRecommendation entity
     * @return formatted class name string
     */
    default String buildClassName(AiRecommendation recommendation) {
        if (recommendation.getClassEntity() != null && recommendation.getClassEntity().getSubject() != null) {
            return recommendation.getClassEntity().getSubject().getName() + " - " + 
                   recommendation.getClassEntity().getGroupCode();
        }
        return null;
    }
}

