package com.viveek.aiclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new grade.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new grade record")
public class CreateGradeRequest {

    @Schema(description = "Class UUID", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7", required = true)
    @NotNull(message = "Class ID is required")
    private UUID classId;

    @Schema(description = "Student UUID", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b", required = true)
    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @Schema(description = "Type of assessment", example = "Quiz", required = true)
    @NotBlank(message = "Assessment kind is required")
    private String assessmentKind;

    @Schema(description = "Name of the specific assessment", example = "Midterm Exam 1")
    private String assessmentName;

    @Schema(description = "Student's score", example = "85.5", required = true, minimum = "0")
    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be greater than or equal to 0")
    private BigDecimal score;

    @Schema(description = "Maximum possible score", example = "100.0", required = true, minimum = "0.01")
    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.01", message = "Max score must be greater than 0")
    private BigDecimal maxScore;

    @Schema(description = "When the grade was assigned", example = "2025-10-11T10:30:00-05:00")
    private ZonedDateTime gradedAt;

    @Schema(
        description = "Additional metadata (JSON object). " +
                      "**Assessment Content (define once per assessment):** Use keys 'assessmentContent', 'content', 'description', or 'assessmentDescription' to describe what the assessment covers. " +
                      "This should be provided when creating the FIRST grade for an assessment. For subsequent grades of the same assessment, assessment content is automatically copied from the first grade. " +
                      "**Teacher Feedback (specific to each student):** Use keys 'feedback', 'teacherFeedback', 'comments', 'notes', or 'teacherComments' to provide personalized feedback for this student. " +
                      "Feedback should be provided for each grade and is specific to the individual student's performance. " +
                      "This metadata is used by AI recommendation generation to provide context-aware suggestions.",
        example = "{\"assessmentContent\": \"Chapter 1-5: Data Structures\", \"feedback\": \"Excellent work on binary trees\", \"notes\": \"Strong understanding\", \"rubric_score\": 4}"
    )
    private Map<String, Object> metadata;
}

