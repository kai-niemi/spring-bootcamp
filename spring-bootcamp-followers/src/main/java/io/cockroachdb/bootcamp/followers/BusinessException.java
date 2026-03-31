package io.cockroachdb.bootcamp.followers;

public class BusinessException extends RuntimeException {
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
