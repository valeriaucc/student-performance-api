package com.viveek.aiclass.dto.request;

import com.viveek.aiclass.domain.model.enums.Semester;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new class section")
public class CreateClassRequest {

    @Schema(description = "Subject UUID", example = "6711adec-edc0-45e6-8d55-b9f28e74cebd", required = true)
    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @Schema(description = "Teacher UUID", example = "b2ff3b86-5c6a-4c19-826e-61696649c4e8", required = true)
    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;

    @Schema(description = "Academic year", example = "2025", required = true, minimum = "2000", maximum = "2100")
    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Max(value = 2100, message = "Year must be at most 2100")
    private Integer year;

    @Schema(description = "Academic semester", example = "SPRING", required = true, allowableValues = {"SPRING", "SUMMER", "FALL", "WINTER"})
    @NotNull(message = "Semester is required")
    private Semester semester;

    @Schema(description = "Group code/section", example = "A", required = true)
    @NotBlank(message = "Group code is required")
    private String groupCode;

    @Schema(description = "Additional metadata", example = "{\"room\": \"Building C, Room 201\", \"capacity\": 30}")
    private Map<String, Object> metadata;
}

