package io.cockroachdb.bootcamp.locking;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.LockingApplication;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {LockingApplication.class})
public class LockingPatternTest extends AbstractIntegrationTest {

    private static <V> V executionTime(Supplier<V> task,
                                       BiConsumer<V, Duration> timeConsumer) {
        Instant start = Instant.now();
        V r = task.get();
        timeConsumer.accept(r, Duration.between(start, Instant.now()));
        return r;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @BeforeAll
    public void beforeAll() {
        executionTime(() -> {
            final AtomicInteger n = new AtomicInteger();

            return n.get();
        }, (count, duration) -> logger.info("%,d products inserted in %s"
                .formatted(count, duration.toString())));
    }
}
