package io.cockroachdb.bootcamp.model;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DurationFormat;
import org.springframework.format.datetime.standard.DurationFormatterUtils;

import jakarta.persistence.LockModeType;

/**
 * Various simulation properties for the sake of demonstrating transaction semantics.
 */
public class Simulation {
    public static Simulation instance() {
        return new Simulation();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Duration commitDelay = Duration.ofMillis(0);

    private LockModeType lockModeType = LockModeType.NONE;

    public Duration getCommitDelay() {
        return commitDelay;
    }

    public Simulation setCommitDelay(Duration commitDelay) {
        this.commitDelay = commitDelay;
        return this;
    }

    public LockModeType getLockModeType() {
        return lockModeType;
    }

    public Simulation setLockModeType(LockModeType lockModeType) {
        this.lockModeType = lockModeType;
        return this;
    }

    public void thinkTime() {
        // Use a fake pause to allow controlling contending updates with retries
        if (getCommitDelay().isPositive()) {
            try {
                logger.warn("Entering {} wait",
                        DurationFormatterUtils.print(getCommitDelay(), DurationFormat.Style.SIMPLE));
                TimeUnit.MILLISECONDS.sleep(getCommitDelay().toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                logger.warn("Proceeding after {} wait",
                        DurationFormatterUtils.print(getCommitDelay(), DurationFormat.Style.SIMPLE));
            }
        }
    }
}
