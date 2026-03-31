package io.cockroachdb.bootcamp.batching;

public class BusinessException extends RuntimeException {
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
