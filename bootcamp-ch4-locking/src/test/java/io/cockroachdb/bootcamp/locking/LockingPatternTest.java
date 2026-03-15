package io.cockroachdb.bootcamp.locking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.AsyncTaskExecutor;

import io.cockroachdb.bootcamp.LockingApplication;
import io.cockroachdb.bootcamp.model.Customer;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {LockingApplication.class})
public abstract class LockingPatternTest extends AbstractIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AsyncTaskExecutor asyncTaskExecutor;

    @BeforeAll
    public void beforeAll() {
        createCustomersAndProducts(100, 5);
    }

    @Order(1)
    @Test
    public void whenPlacingOrdersConcurrently_thenForceExecuteAsClusterSingleton() {
        sampleDataService.withRandomCustomersAndProducts(
                100, 5, (customers, products) -> {
                    List<CompletableFuture<?>> futures = new ArrayList<>();

                    for (Customer customer : customers) {
                        PurchaseOrder.Builder orderBuilder = PurchaseOrder
                                .builder()
                                .withCustomer(customer);

                        products.forEach(product ->
                                orderBuilder.andOrderItem()
                                        .withProductId(product.getId())
                                        .withProductSku(product.getSku())
                                        .withUnitPrice(product.getPrice())
                                        .withQuantity(2)
                                        .then()
                        );

                        CompletableFuture<?> f = CompletableFuture.supplyAsync(() -> {
                            orderService.placeOrder(orderBuilder.build());
                            return null;
                        }, asyncTaskExecutor);

                        futures.add(f);
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                    return null;
                });
    }
}
