package com.viveek.aiclass.dto.request;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new enrollment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for enrolling a student in a class")
public class CreateEnrollmentRequest {

    @Schema(description = "Class UUID", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7", required = true)
    @NotNull(message = "Class ID is required")
    private UUID classId;

    @Schema(description = "Student UUID", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b", required = true)
    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @Schema(description = "Enrollment status (defaults to ACTIVE)", example = "ACTIVE", allowableValues = {"ACTIVE", "DROPPED", "COMPLETED"})
    private EnrollmentStatus status;
}

