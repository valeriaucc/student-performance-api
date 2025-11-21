package com.viveek.aiclass.dto.response;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for AiRecommendation entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI recommendation details response")
public class RecommendationResponse {

    @Schema(description = "Recommendation unique identifier", example = "9c5e8d7f-4a3b-2c1d-0e9f-8a7b6c5d4e3f")
    private UUID id;
    
    @Schema(description = "Class UUID", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7")
    private UUID classId;
    
    @Schema(description = "Class name (subject + group)", example = "CS101-A")
    private String className;
    
    @Schema(description = "Grade UUID (if recommendation is linked to a specific assessment)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID gradeId;
    
    @Schema(description = "Recipient user UUID", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b")
    private UUID recipientId;
    
    @Schema(description = "Recipient name", example = "John Doe")
    private String recipientName;
    
    @Schema(description = "Target audience", example = "STUDENT")
    private RecommendationAudience audience;
    
    @Schema(description = "Recommendation message", example = "Focus on chapter 3 exercises to improve your understanding")
    private String message;
    
    @Schema(description = "Additional metadata", example = "{\"confidence\": 0.87, \"category\": \"study_strategy\"}")
    private Map<String, Object> metadata;
    
    @Schema(description = "Creation timestamp", example = "2025-10-11T15:00:00-05:00")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2025-10-11T15:00:00-05:00")
    private ZonedDateTime updatedAt;
}

