package com.viveek.aiclass.dto.request;

import com.viveek.aiclass.domain.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating user information (all fields optional)")
public class UpdateUserRequest {

    @Schema(description = "User's full name", example = "Jane Smith")
    private String fullName;

    @Schema(
        description = "User's email address (will be normalized to lowercase and trimmed)", 
        example = "jane.smith@example.com"
    )
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "User role (lowercase)", example = "teacher", allowableValues = {"teacher", "student"})
    private UserRole role;

    @Schema(description = "Additional metadata", example = "{\"department\": \"Mathematics\"}")
    private Map<String, Object> metadata;
}

