package io.cockroachdb.bootcamp.performance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.core.task.SimpleAsyncTaskExecutor;

public abstract class ConcurrencyUtils {
    private ConcurrencyUtils() {
    }

    public static List<BigDecimal> runConcurrentlyAndCollect(int threads, Callable<BigDecimal> task) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        // If this is limit is removed ensure the connection-timeout value is high enough,
        // or you risk getting timeouts (signaling pool exhaustion).
        executor.setConcurrencyLimit(32);

        List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();

        IntStream.rangeClosed(1, threads).forEach(value ->
                futures.add(executor.submitCompletable(task)));

        try {
            CompletableFuture<List<BigDecimal>> allFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture<?>[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()));
            return allFutures.join();
        } catch (CompletionException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
