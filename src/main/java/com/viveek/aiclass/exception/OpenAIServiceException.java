package com.viveek.aiclass.exception;

/**
 * Exception thrown when OpenAI API service encounters an error.
 * This could be due to API failures, network issues, or invalid responses.
 */
public class OpenAIServiceException extends RuntimeException {

    public OpenAIServiceException(String message) {
        super(message);
    }

    public OpenAIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

