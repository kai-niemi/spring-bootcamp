package io.cockroachdb.bootcamp.patterns.idempotency;

public class BusinessException extends RuntimeException {
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
