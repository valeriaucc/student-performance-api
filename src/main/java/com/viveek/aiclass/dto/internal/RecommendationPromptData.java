package com.viveek.aiclass.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Internal DTO for passing data to OpenAI service for recommendation generation.
 * Contains all necessary information extracted from a Grade entity to build the prompt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationPromptData {

    /**
     * Subject name (e.g., "Mathematics", "Computer Science")
     */
    private String subjectName;

    /**
     * Assessment content/description extracted from metadata
     */
    private String assessmentContent;

    /**
     * Teacher feedback extracted from metadata
     */
    private String feedback;

    /**
     * Student's score on the assessment
     */
    private BigDecimal score;

    /**
     * Maximum possible score
     */
    private BigDecimal maxScore;

    /**
     * Assessment name (e.g., "Midterm Exam 1")
     */
    private String assessmentName;

    /**
     * Assessment kind/type (e.g., "Quiz", "Exam", "Assignment")
     */
    private String assessmentKind;

    /**
     * Calculates the percentage score.
     * 
     * @return percentage as BigDecimal, or null if maxScore is null or zero
     */
    public BigDecimal getPercentage() {
        if (maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        if (score == null) {
            return null;
        }
        return score.divide(maxScore, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}

