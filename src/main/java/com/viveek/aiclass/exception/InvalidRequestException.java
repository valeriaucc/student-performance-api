package com.viveek.aiclass.exception;

/**
 * Exception thrown when a request contains invalid data or logic.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

