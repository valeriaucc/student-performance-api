package com.viveek.aiclass.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Simplified recommendation summary for inclusion in Grade responses.
 * Contains only essential fields to avoid bloating the grade response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Simplified recommendation summary")
public class RecommendationSummary {

    @Schema(description = "Recommendation unique identifier", example = "9c5e8d7f-4a3b-2c1d-0e9f-8a7b6c5d4e3f")
    private UUID id;

    @Schema(description = "Recommendation message", example = "Focus on chapter 3 exercises to improve your understanding")
    private String message;
}

