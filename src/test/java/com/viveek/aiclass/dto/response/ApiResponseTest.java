package com.viveek.aiclass.dto.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void success_WithData_CreatesSuccessResponse() {
        String data = "test data";
        
        ApiResponse<String> response = ApiResponse.success(data);
        
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operation successful");
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_WithMessageAndData_CreatesSuccessResponse() {
        String message = "Custom success message";
        String data = "test data";
        
        ApiResponse<String> response = ApiResponse.success(message, data);
        
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void error_CreatesErrorResponse() {
        String errorMessage = "Error occurred";
        
        ApiResponse<String> response = ApiResponse.error(errorMessage);
        
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void builder_CreatesCustomResponse() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Custom message")
                .data("custom data")
                .build();
        
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Custom message");
        assertThat(response.getData()).isEqualTo("custom data");
    }

    @Test
    void allArgsConstructor_CreatesResponse() {
        String data = "test";
        ApiResponse<String> response = new ApiResponse<>(true, "message", data, null);
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("message");
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    void noArgsConstructor_CreatesEmptyResponse() {
        ApiResponse<String> response = new ApiResponse<>();
        
        assertThat(response).isNotNull();
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("test");
        response.setData("data");
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("test");
        assertThat(response.getData()).isEqualTo("data");
    }
}


