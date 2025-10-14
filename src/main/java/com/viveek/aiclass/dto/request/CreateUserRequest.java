package com.viveek.aiclass.dto.request;

import com.viveek.aiclass.domain.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new user")
public class CreateUserRequest {

    @Schema(description = "Supabase Auth user ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull(message = "Auth user ID is required")
    private UUID authUserId;

    @Schema(description = "User's full name", example = "John Doe", required = true)
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Schema(
        description = "User's email address (will be normalized to lowercase and trimmed)", 
        example = "john.doe@example.com", 
        required = true
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "User role (lowercase)", example = "teacher", required = true, allowableValues = {"teacher", "student"})
    @NotNull(message = "Role is required")
    private UserRole role;

    @Schema(description = "Additional metadata as key-value pairs", example = "{\"department\": \"Computer Science\"}")
    private Map<String, Object> metadata;
}

