package org.acme.domain.minio.exception;

public class MinioProcessingException extends RuntimeException {
    public MinioProcessingException(String message) {
        super(message);
    }

    public MinioProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}