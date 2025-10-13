package com.viveek.aiclass.dto.request;

import com.viveek.aiclass.domain.model.enums.Semester;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for updating an existing class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating class information (all fields optional)")
public class UpdateClassRequest {

    @Schema(description = "Subject UUID", example = "6711adec-edc0-45e6-8d55-b9f28e74cebd")
    private UUID subjectId;

    @Schema(description = "Teacher UUID", example = "b2ff3b86-5c6a-4c19-826e-61696649c4e8")
    private UUID teacherId;

    @Schema(description = "Academic year", example = "2026", minimum = "2000", maximum = "2100")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Max(value = 2100, message = "Year must be at most 2100")
    private Integer year;

    @Schema(description = "Academic semester", example = "FALL", allowableValues = {"SPRING", "SUMMER", "FALL", "WINTER"})
    private Semester semester;

    @Schema(description = "Group code/section", example = "B")
    private String groupCode;

    @Schema(description = "Additional metadata", example = "{\"room\": \"Building D, Room 305\"}")
    private Map<String, Object> metadata;
}

