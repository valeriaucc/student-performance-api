package com.viveek.aiclass.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void resourceNotFoundException_WithParameters() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "id", "123");
        
        assertThat(exception.getMessage()).contains("User");
        assertThat(exception.getMessage()).contains("id");
        assertThat(exception.getMessage()).contains("123");
    }

    @Test
    void resourceAlreadyExistsException_WithParameters() {
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException("User", "email", "test@test.com");
        
        assertThat(exception.getMessage()).contains("User");
        assertThat(exception.getMessage()).contains("email");
        assertThat(exception.getMessage()).contains("test@test.com");
    }

    @Test
    void invalidRequestException_WithMessage() {
        InvalidRequestException exception = new InvalidRequestException("Invalid input");
        
        assertThat(exception.getMessage()).isEqualTo("Invalid input");
    }

    @Test
    void businessException_WithMessage() {
        BusinessException exception = new BusinessException("Business rule violated");
        
        assertThat(exception.getMessage()).isEqualTo("Business rule violated");
    }
}


