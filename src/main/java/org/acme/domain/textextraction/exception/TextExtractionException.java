package org.acme.domain.textextraction.exception;

public class TextExtractionException extends RuntimeException {
    public TextExtractionException(String message) {
        super(message);
    }
    
    public TextExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}