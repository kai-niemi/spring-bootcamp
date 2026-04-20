package io.cockroachdb.bootcamp.interceptor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.RollbackException;

import io.cockroachdb.bootcamp.aspect.ExceptionClassifier;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;
import io.cockroachdb.bootcamp.repository.MetadataUtils;

/**
 * @author Kai Niemi
 */
public class SimpleInterceptorTest extends AbstractInterceptorTest {
    @Order(0)
    @Test
    public void whenCheckingIsolationLevel_thenExpectSerializable() {
        Assertions.assertEquals("SERIALIZABLE", MetadataUtils.databaseIsolation(dataSource).toUpperCase());
    }

    @Order(1)
    @Test
    public void givenRetries_whenReadModifyWriteConcurrently_thenExpectSuccess() {
        CompletableFuture<?> t1 = CompletableFuture.runAsync(() -> {
            orderService.updateOrder(purchaseOrderId1,
                    ShipmentStatus.placed,
                    ShipmentStatus.confirmed,
                    Simulation.instance()
                            .setCommitDelay(Duration.ofSeconds(5))); // Lets' think for 5s before write+commit
        });

        CompletableFuture<?> t2 = CompletableFuture.runAsync(() -> {
            orderService.updateOrder(purchaseOrderId1,
                    ShipmentStatus.placed,
                    ShipmentStatus.cancelled,
                    Simulation.instance()
                            .setCommitDelay(Duration.ofSeconds(5))); // Let's wait here also for a predicable outcome
        }, CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)); // Ensure T2 starts after T1

        // Expect both T1 and T2 to succeed, which will not happen w/o retries
        CompletableFuture.allOf(t1, t2).join();

        PurchaseOrder purchaseOrder = orderService.findOrderById(purchaseOrderId1).orElseThrow();
        Assertions.assertEquals(ShipmentStatus.confirmed, purchaseOrder.getStatus());
    }

    @Autowired
    private ExceptionClassifier exceptionClassifier;

    @Order(2)
    @Test
    public void givenNoRetries_whenReadModifyWriteConcurrently_thenExpectFailure() {
        // Disable retries momentarily
        exceptionClassifier.setEnabled(false);

        CompletableFuture<?> t1 = CompletableFuture.runAsync(() -> {
            orderService.updateOrder(purchaseOrderId2,
                    ShipmentStatus.placed,
                    ShipmentStatus.confirmed,
                    Simulation.instance()
                            .setCommitDelay(Duration.ofSeconds(5)));
        });

        CompletableFuture<?> t2 = CompletableFuture.runAsync(() -> {
            orderService.updateOrder(purchaseOrderId2,
                    ShipmentStatus.placed,
                    ShipmentStatus.cancelled,
                    Simulation.instance()
                            .setCommitDelay(Duration.ofSeconds(5)));
        }, CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS));

        // Expect only T1 to succeed since were not catching the transient error
        t1.join();

        CompletionException ex = Assertions.assertThrows(CompletionException.class, t2::join);
        Assertions.assertInstanceOf(RollbackException.class, ex.getCause());

        PurchaseOrder purchaseOrder = orderService.findOrderById(purchaseOrderId2).orElseThrow();
        Assertions.assertEquals(ShipmentStatus.confirmed, purchaseOrder.getStatus());
    }
}
