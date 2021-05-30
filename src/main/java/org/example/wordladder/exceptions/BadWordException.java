package org.example.wordladder.exceptions;

public class BadWordException extends DictionaryLoadErrorException {
    public BadWordException(String message) {
        super(message);
    }
}
