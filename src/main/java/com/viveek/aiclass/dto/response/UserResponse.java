package com.viveek.aiclass.dto.response;

import com.viveek.aiclass.domain.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for User entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User details response")
public class UserResponse {

    @Schema(description = "User unique identifier", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b")
    private UUID id;
    
    @Schema(description = "Supabase Auth user ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID authUserId;
    
    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User role", example = "STUDENT")
    private UserRole role;
    
    @Schema(description = "Additional metadata", example = "{\"department\": \"Computer Science\"}")
    private Map<String, Object> metadata;
    
    @Schema(description = "Creation timestamp", example = "2025-10-11T10:30:00-05:00")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2025-10-11T14:45:00-05:00")
    private ZonedDateTime updatedAt;
}

