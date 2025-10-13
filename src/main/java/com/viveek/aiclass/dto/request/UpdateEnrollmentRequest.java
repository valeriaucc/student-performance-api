package com.viveek.aiclass.dto.request;

import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing enrollment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating enrollment status")
public class UpdateEnrollmentRequest {

    @Schema(description = "New enrollment status", example = "COMPLETED", required = true, allowableValues = {"ACTIVE", "DROPPED", "COMPLETED"})
    @NotNull(message = "Status is required")
    private EnrollmentStatus status;
}

