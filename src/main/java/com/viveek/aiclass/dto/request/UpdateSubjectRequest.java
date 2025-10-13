package com.viveek.aiclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing subject.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating subject information (all fields optional)")
public class UpdateSubjectRequest {

    @Schema(description = "Subject code", example = "CS102")
    private String code;

    @Schema(description = "Subject name", example = "Data Structures")
    private String name;

    @Schema(description = "Subject description", example = "Advanced data structures and algorithms")
    private String description;
}

