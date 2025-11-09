package com.viveek.aiclass.dto.response;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Response DTO for Enrollment entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Enrollment details response")
public class EnrollmentResponse {

    @Schema(description = "Enrollment unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID id;
    
    @Schema(description = "Class UUID", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7")
    private UUID classId;
    
    @Schema(description = "Class name (subject + group)", example = "CS101-A")
    private String className;
    
    @Schema(description = "Student UUID", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b")
    private UUID studentId;
    
    @Schema(description = "Student name", example = "John Doe")
    private String studentName;
    
    @Schema(description = "Student email", example = "john.doe@example.com")
    private String studentEmail;
    
    @Schema(description = "Enrollment status", example = "ACTIVE")
    private EnrollmentStatus status;
    
    @Schema(description = "Enrollment timestamp", example = "2025-09-01T08:00:00-05:00")
    private ZonedDateTime enrolledAt;
    
    @Schema(description = "Creation timestamp", example = "2025-09-01T08:00:00-05:00")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2025-10-11T14:00:00-05:00")
    private ZonedDateTime updatedAt;
}

