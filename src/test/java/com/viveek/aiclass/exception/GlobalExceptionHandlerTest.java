package com.viveek.aiclass.exception;

import com.viveek.aiclass.dto.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    void handleResourceNotFoundException_ReturnsNotFound() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "id", "123");
        
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("User");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleResourceAlreadyExistsException_ReturnsConflict() {
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException("User", "email", "test@test.com");
        
        ResponseEntity<ErrorResponse> response = handler.handleResourceAlreadyExistsException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("User");
    }

    @Test
    void handleInvalidRequestException_ReturnsBadRequest() {
        InvalidRequestException exception = new InvalidRequestException("Invalid data");
        
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequestException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid data");
    }

    @Test
    void handleBusinessException_ReturnsUnprocessableEntity() {
        BusinessException exception = new BusinessException("Business rule violation");
        
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getError()).isEqualTo("Unprocessable Entity");
        assertThat(response.getBody().getMessage()).isEqualTo("Business rule violation");
    }

    @Test
    void handleValidationException_ReturnsBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        
        FieldError error1 = new FieldError("object", "field1", "error message 1");
        FieldError error2 = new FieldError("object", "field2", "error message 2");
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error1, error2));
        
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails().get(0)).contains("field1");
        assertThat(response.getBody().getDetails().get(1)).contains("field2");
    }

    @Test
    void handleGlobalException_ReturnsInternalServerError() {
        Exception exception = new RuntimeException("Unexpected error");
        
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).contains("Unexpected error");
    }
}


