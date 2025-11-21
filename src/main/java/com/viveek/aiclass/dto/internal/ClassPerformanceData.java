package com.viveek.aiclass.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Internal DTO for passing class performance data to OpenAI service for teacher recommendation generation.
 * Contains aggregated performance metrics and grade details for a class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassPerformanceData {

    /**
     * Subject name
     */
    private String subjectName;

    /**
     * Class identifier (subject + group code)
     */
    private String className;

    /**
     * Total number of students in the class
     */
    private Integer totalStudents;

    /**
     * Total number of assessments/grades
     */
    private Integer totalAssessments;

    /**
     * Average class score (across all grades)
     */
    private BigDecimal averageScore;

    /**
     * Average percentage (across all grades)
     */
    private BigDecimal averagePercentage;

    /**
     * List of assessment summaries with performance data
     */
    private List<AssessmentSummary> assessments;

    /**
     * Performance distribution (e.g., "Excellent: 5, Good: 10, Needs Improvement: 3")
     */
    private Map<String, Integer> performanceDistribution;

    /**
     * Common weak areas identified across assessments
     */
    private List<String> commonWeakAreas;

    /**
     * Common strong areas identified across assessments
     */
    private List<String> commonStrongAreas;

    /**
     * Summary of assessment content/topics covered
     */
    private String assessmentContentSummary;

    /**
     * Summary of teacher feedback patterns
     */
    private String feedbackSummary;

    /**
     * Inner class for assessment summary
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentSummary {
        private String assessmentName;
        private String assessmentKind;
        private BigDecimal averageScore;
        private BigDecimal averagePercentage;
        private Integer studentCount;
        private String content;
    }
}

