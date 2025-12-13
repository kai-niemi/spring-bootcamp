package io.cockroachdb.bootcamp.aspect;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.resilience.retry.MethodRetryPredicate;

public class TransientExceptionClassifier implements MethodRetryPredicate {
    private final ExceptionClassifier exceptionClassifier;

    public TransientExceptionClassifier(@Autowired ExceptionClassifier exceptionClassifier) {
        this.exceptionClassifier = exceptionClassifier;
    }

    @Override
    public boolean shouldRetry(Method method, Throwable ex) {
        return exceptionClassifier.shouldRetry(method, ex);
    }
}
