package org.example.wordladder.exceptions;

public class NoResourceForDictionaryException extends DictionaryLoadErrorException {
    public NoResourceForDictionaryException(String message) {
        super(message);
    }
}
