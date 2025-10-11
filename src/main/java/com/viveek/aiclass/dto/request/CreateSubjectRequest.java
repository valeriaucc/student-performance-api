package com.viveek.aiclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new subject.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new subject")
public class CreateSubjectRequest {

    @Schema(description = "Unique subject code", example = "CS101", required = true)
    @NotBlank(message = "Subject code is required")
    private String code;

    @Schema(description = "Subject name", example = "Introduction to Computer Science", required = true)
    @NotBlank(message = "Subject name is required")
    private String name;

    @Schema(description = "Subject description", example = "Fundamental concepts of programming")
    private String description;
}

