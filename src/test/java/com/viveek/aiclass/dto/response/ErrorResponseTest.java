package com.viveek.aiclass.dto.response;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void builder_CreatesErrorResponse() {
        ZonedDateTime now = ZonedDateTime.now();
        List<String> details = Arrays.asList("detail1", "detail2");
        
        ErrorResponse response = ErrorResponse.builder()
                .message("Error message")
                .status(400)
                .error("Bad Request")
                .path("/api/test")
                .details(details)
                .timestamp(now)
                .build();
        
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Error message");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Bad Request");
        assertThat(response.getPath()).isEqualTo("/api/test");
        assertThat(response.getDetails()).hasSize(2);
        assertThat(response.getTimestamp()).isEqualTo(now);
    }

    @Test
    void allArgsConstructor_CreatesErrorResponse() {
        ZonedDateTime now = ZonedDateTime.now();
        ErrorResponse response = new ErrorResponse(
                "message", 
                404, 
                "Not Found", 
                "/api/users/1", 
                null, 
                now
        );
        
        assertThat(response.getMessage()).isEqualTo("message");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getDetails()).isNull();
    }

    @Test
    void noArgsConstructor_CreatesEmptyResponse() {
        ErrorResponse response = new ErrorResponse();
        assertThat(response).isNotNull();
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        ErrorResponse response = new ErrorResponse();
        ZonedDateTime now = ZonedDateTime.now();
        
        response.setMessage("test message");
        response.setStatus(500);
        response.setError("Internal Server Error");
        response.setPath("/test");
        response.setDetails(Arrays.asList("detail"));
        response.setTimestamp(now);
        
        assertThat(response.getMessage()).isEqualTo("test message");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getError()).isEqualTo("Internal Server Error");
        assertThat(response.getPath()).isEqualTo("/test");
        assertThat(response.getDetails()).containsExactly("detail");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }
}

