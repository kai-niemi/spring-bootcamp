package io.cockroachdb.bootcamp.patterns.parallel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.patterns.AsciiArt;
import io.cockroachdb.bootcamp.Chapter4Application;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {Chapter4Application.class})
public class ParallelQueryPatternTest extends AbstractIntegrationTest {
    private static final int PRODUCT_INVENTORY = 250;

    private static final int NUM_PRODUCTS_PER_COUNTRY = 50_000;

    private static final List<String> COUNTRIES
            = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

    private static <V> V executionTime(Supplier<V> task,
                                       BiConsumer<V, Duration> timeConsumer) {
        Instant start = Instant.now();
        V r = task.get();
        timeConsumer.accept(r, Duration.between(start, Instant.now()));
        return r;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<BigDecimal> observedTotals = new HashSet<>();

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeAll
    public void beforeAll() {
        executionTime(() -> {
            final AtomicInteger n = new AtomicInteger();

            COUNTRIES.forEach(country -> {
                AsciiArt.printProgressBar(n.incrementAndGet(), COUNTRIES.size(),
                        "Inserting %d products for country '%s'"
                                .formatted(NUM_PRODUCTS_PER_COUNTRY, country));
                inventoryRepository.insertProducts(PRODUCT_INVENTORY,
                        country, NUM_PRODUCTS_PER_COUNTRY);
            });

            return n.get() * NUM_PRODUCTS_PER_COUNTRY;
        }, (count, duration) -> logger.info("%,d products inserted in %s"
                .formatted(count, duration.toString())));
    }

    @Order(1)
    @RepeatedTest(5)
    public void whenExecutingAggregationQuery_thenExpectTotalSum() {
        BigDecimal total = executionTime(() -> {
            logger.info("Executing aggregate query without predicate..");
            return inventoryRepository.sumInventory();
        }, (sum, duration) ->
                logger.info("Total: %s Time: %s".formatted(sum, duration.toString())));

        observedTotals.add(total);
    }

    @Order(2)
    @RepeatedTest(5)
    public void whenExecutingAggregationQueryInParallel_thenExpectTotalSum() {
        BigDecimal total = executionTime(() -> {
            try (ForkJoinPool forkJoinPool = ForkJoinPool.commonPool()) {
                logger.info("Executing aggregate query using parallel FJP..");
                SumInventoryTask sumInventoryTask
                        = new SumInventoryTask(COUNTRIES, inventoryRepository);
                forkJoinPool.invoke(sumInventoryTask);
                return sumInventoryTask.join();
            }
        }, (sum, duration) ->
                logger.info("Total: %s Time: %s".formatted(sum, duration.toString())));

        observedTotals.add(total);
    }

    @Order(3)
    @Test
    public void whenWrapping_thenExpectSameTotalSum() {
        Assertions.assertEquals(1, observedTotals.size());
    }
}
