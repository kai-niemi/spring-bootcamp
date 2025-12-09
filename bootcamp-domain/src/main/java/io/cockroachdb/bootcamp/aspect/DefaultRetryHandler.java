package io.cockroachdb.bootcamp.aspect;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.bootcamp.annotation.Idempotent;

public class DefaultRetryHandler implements RetryHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean enabled = true;

    @Override
    public boolean shouldRetry(Signature signature, Throwable throwable) {
        if (enabled && throwable instanceof SQLException sqlException) {
            // 40001 (PSQLState.SERIALIZATION_FAILURE) is the only state code we are looking for in terms of safe retries.
            // Other codes can be safe to retry if the operations performed are idempotent (UPDATE/UPSERT/DELETE).
            if ("40001".equals(sqlException.getSQLState())) {
                return true;
            }

            // Check for idempotent signal
            Idempotent idempotent = TransactionRetryAspect.findAnnotation(signature, Idempotent.class);
            if (idempotent != null) {
                return Arrays.stream(idempotent.transientSQLStates())
                        .collect(Collectors.toSet()).contains(sqlException.getSQLState());
            }
        }
        return false;
    }

    @Override
    public void setEnableRetry(boolean enableRetry) {
        this.enabled = enableRetry;
    }

    @Override
    public void beforeRetry(Signature signature, Throwable throwable, int methodCalls, long maxBackoff) {
        if (throwable instanceof SQLException sqlException) {
            try {
                final long backoffMillis = Math.min((long) (Math.pow(2, methodCalls) + ThreadLocalRandom.current()
                        .nextInt(1000)), maxBackoff);

                logger.warn("Transient SQL error code [%s] for method [%s] attempt (%d) backoff %s ms: %s"
                        .formatted(
                                sqlException.getSQLState(),
                                signature.toShortString(),
                                methodCalls,
                                backoffMillis,
                                sqlException.getMessage()));
                TimeUnit.MILLISECONDS.sleep(backoffMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            logger.warn("Non-transient error for method [%s]: %s".formatted(
                    signature.toString(),
                    throwable.getMessage()));
        }
    }

    @Override
    public void afterRetry(Signature signature, Throwable throwable, int methodCalls, Duration elapsedTime) {
        logger.info("Recovered from exception in method [%s] after attempt (%d) time spent: %s"
                .formatted(signature.toShortString(),
                        methodCalls,
                        elapsedTime.toString()));
    }
}
