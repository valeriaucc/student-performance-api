package com.viveek.aiclass.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Response DTO for Grade entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Grade record details response")
public class GradeResponse {

    @Schema(description = "Grade unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID id;
    
    @Schema(description = "Class UUID", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7")
    private UUID classId;
    
    @Schema(description = "Class name (subject + group)", example = "CS101-A")
    private String className;
    
    @Schema(description = "Student UUID", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b")
    private UUID studentId;
    
    @Schema(description = "Student name", example = "John Doe")
    private String studentName;
    
    @Schema(description = "Type of assessment", example = "Quiz")
    private String assessmentKind;
    
    @Schema(description = "Name of the specific assessment", example = "Midterm Exam 1")
    private String assessmentName;
    
    @Schema(description = "Student's score", example = "85.5")
    private BigDecimal score;
    
    @Schema(description = "Maximum possible score", example = "100.0")
    private BigDecimal maxScore;
    
    @Schema(description = "Percentage score (calculated)", example = "85.5")
    private BigDecimal percentage;
    
    @Schema(description = "When the grade was assigned", example = "2025-10-11T10:30:00-05:00")
    private ZonedDateTime gradedAt;
    
    @Schema(description = "Creation timestamp", example = "2025-10-11T10:30:00-05:00")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2025-10-11T14:45:00-05:00")
    private ZonedDateTime updatedAt;
}

