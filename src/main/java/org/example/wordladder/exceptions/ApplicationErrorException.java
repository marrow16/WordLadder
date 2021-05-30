package org.example.wordladder.exceptions;

public class ApplicationErrorException extends RuntimeException {
    public ApplicationErrorException(String message) {
        super(message);
    }

    public ApplicationErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
