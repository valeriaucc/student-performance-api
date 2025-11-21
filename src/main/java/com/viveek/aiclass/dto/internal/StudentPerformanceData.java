package com.viveek.aiclass.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Internal DTO for passing student performance data to OpenAI service for teacher recommendation generation.
 * Contains performance metrics and grade details for a specific student.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPerformanceData {

    /**
     * Student name
     */
    private String studentName;

    /**
     * Subject name
     */
    private String subjectName;

    /**
     * Class identifier (subject + group code)
     */
    private String className;

    /**
     * Total number of assessments for this student
     */
    private Integer totalAssessments;

    /**
     * Average score across all assessments
     */
    private BigDecimal averageScore;

    /**
     * Average percentage across all assessments
     */
    private BigDecimal averagePercentage;

    /**
     * Performance trend (improving, declining, stable)
     */
    private String performanceTrend;

    /**
     * List of recent assessments with details
     */
    private List<StudentAssessmentSummary> assessments;

    /**
     * Identified weak areas based on performance
     */
    private List<String> weakAreas;

    /**
     * Identified strong areas based on performance
     */
    private List<String> strongAreas;

    /**
     * Summary of teacher feedback patterns
     */
    private String feedbackSummary;

    /**
     * Inner class for student assessment summary
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentAssessmentSummary {
        private String assessmentName;
        private String assessmentKind;
        private BigDecimal score;
        private BigDecimal maxScore;
        private BigDecimal percentage;
        private String content;
        private String feedback;
    }
}

