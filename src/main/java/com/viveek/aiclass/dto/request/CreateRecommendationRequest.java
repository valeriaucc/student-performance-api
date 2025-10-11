package com.viveek.aiclass.dto.request;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new AI recommendation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating an AI-generated recommendation")
public class CreateRecommendationRequest {

    @Schema(description = "Class UUID", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7", required = true)
    @NotNull(message = "Class ID is required")
    private UUID classId;

    @Schema(description = "Recipient user UUID", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b", required = true)
    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;

    @Schema(description = "Target audience for the recommendation", example = "STUDENT", required = true, allowableValues = {"TEACHER", "STUDENT"})
    @NotNull(message = "Audience is required")
    private RecommendationAudience audience;

    @Schema(description = "Recommendation message content", example = "Focus on chapter 3 exercises to improve your understanding", required = true)
    @NotBlank(message = "Message is required")
    private String message;

    @Schema(description = "Additional metadata", example = "{\"confidence\": 0.87, \"category\": \"study_strategy\"}")
    private Map<String, Object> metadata;
}

