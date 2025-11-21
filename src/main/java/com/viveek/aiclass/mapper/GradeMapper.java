package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.AiRecommendation;
import com.viveek.aiclass.domain.model.Grade;
import com.viveek.aiclass.dto.response.GradeResponse;
import com.viveek.aiclass.dto.response.RecommendationSummary;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MapStruct mapper for Grade entity conversions.
 * Handles mapping between Grade entities and DTOs with custom calculations for percentage.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeMapper {

    /**
     * Maps Grade entity to GradeResponse DTO.
     * Includes custom calculations for percentage and class name formatting.
     *
     * @param grade the Grade entity
     * @return GradeResponse DTO
     */
    @Mapping(source = "classEntity.id", target = "classId")
    @Mapping(target = "className", expression = "java(buildClassName(grade))")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentName")
    @Mapping(target = "percentage", expression = "java(calculatePercentage(grade))")
    @Mapping(target = "recommendation", ignore = true)
    GradeResponse toResponse(Grade grade);
    
    /**
     * Maps AiRecommendation to RecommendationSummary.
     *
     * @param recommendation the AiRecommendation entity
     * @return RecommendationSummary DTO
     */
    default RecommendationSummary toRecommendationSummary(AiRecommendation recommendation) {
        if (recommendation == null) {
            return null;
        }
        return RecommendationSummary.builder()
                .id(recommendation.getId())
                .message(recommendation.getMessage())
                .build();
    }

    /**
     * Calculates percentage score from grade's score and maxScore.
     *
     * @param grade the Grade entity
     * @return calculated percentage or null if calculation not possible
     */
    default BigDecimal calculatePercentage(Grade grade) {
        if (grade.getScore() != null && grade.getMaxScore() != null && 
            grade.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
            return grade.getScore()
                    .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /**
     * Builds a formatted class name from grade's class and subject information.
     *
     * @param grade the Grade entity
     * @return formatted class name string
     */
    default String buildClassName(Grade grade) {
        if (grade.getClassEntity() != null && grade.getClassEntity().getSubject() != null) {
            return grade.getClassEntity().getSubject().getName() + " - " + 
                   grade.getClassEntity().getGroupCode();
        }
        return null;
    }
}


