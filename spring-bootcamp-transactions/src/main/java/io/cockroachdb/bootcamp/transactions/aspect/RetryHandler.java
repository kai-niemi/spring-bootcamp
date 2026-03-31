package io.cockroachdb.bootcamp.transactions.aspect;

import java.time.Duration;

import org.aspectj.lang.Signature;

@FunctionalInterface
public interface RetryHandler {
    boolean shouldRetry(Signature method, Throwable throwable);

    default void beforeRetry(Signature method,
                             Throwable throwable,
                             int methodCalls,
                             long maxBackoff) {

    }

    default void afterRetry(Signature method,
                            Throwable throwable,
                            int methodCalls,
                            Duration elapsedTime) {
    }
}
