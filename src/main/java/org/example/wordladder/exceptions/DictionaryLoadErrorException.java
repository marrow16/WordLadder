package org.example.wordladder.exceptions;

public class DictionaryLoadErrorException extends ApplicationErrorException {
    public DictionaryLoadErrorException(String message) {
        super(message);
    }

    public DictionaryLoadErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
