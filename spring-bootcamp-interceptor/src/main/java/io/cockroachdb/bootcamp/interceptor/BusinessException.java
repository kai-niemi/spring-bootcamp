package io.cockroachdb.bootcamp.interceptor;

public class BusinessException extends RuntimeException {
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
