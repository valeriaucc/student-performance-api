package com.viveek.aiclass.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Response DTO for Subject entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Subject details response")
public class SubjectResponse {

    @Schema(description = "Subject unique identifier", example = "6711adec-edc0-45e6-8d55-b9f28e74cebd")
    private UUID id;
    
    @Schema(description = "Unique subject code", example = "CS101")
    private String code;
    
    @Schema(description = "Subject name", example = "Introduction to Computer Science")
    private String name;
    
    @Schema(description = "Subject description", example = "Fundamental concepts of programming")
    private String description;
    
    @Schema(description = "Creation timestamp", example = "2025-10-11T10:00:00-05:00")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2025-10-11T14:30:00-05:00")
    private ZonedDateTime updatedAt;
}

