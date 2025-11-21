package com.viveek.aiclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Request DTO for updating an existing grade.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating grade information (all fields optional)")
public class UpdateGradeRequest {

    @Schema(description = "Type of assessment", example = "Final Exam")
    private String assessmentKind;

    @Schema(description = "Name of the specific assessment", example = "Final Exam - Winter 2025")
    private String assessmentName;

    @Schema(description = "Student's score", example = "92.0", minimum = "0")
    @DecimalMin(value = "0.0", message = "Score must be greater than or equal to 0")
    private BigDecimal score;

    @Schema(description = "Maximum possible score", example = "100.0", minimum = "0.01")
    @DecimalMin(value = "0.01", message = "Max score must be greater than 0")
    private BigDecimal maxScore;

    @Schema(description = "When the grade was assigned", example = "2025-10-15T14:30:00-05:00")
    private ZonedDateTime gradedAt;

    @Schema(
        description = "Additional metadata (JSON object). " +
                      "Use keys 'assessmentContent', 'content', 'description', or 'assessmentDescription' for assessment content. " +
                      "Use keys 'feedback', 'teacherFeedback', 'comments', 'notes', or 'teacherComments' for teacher feedback. " +
                      "This metadata is used by AI recommendation generation to provide context-aware suggestions.",
        example = "{\"assessmentContent\": \"Chapter 1-5: Data Structures\", \"feedback\": \"Great improvement\", \"notes\": \"Updated score\", \"rubric_score\": 4}"
    )
    private Map<String, Object> metadata;
}

