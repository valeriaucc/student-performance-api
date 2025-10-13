package com.viveek.aiclass.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Error response DTO for API exceptions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String message;
    private int status;
    private String error;
    private String path;
    private List<String> details;
    private ZonedDateTime timestamp;
}

