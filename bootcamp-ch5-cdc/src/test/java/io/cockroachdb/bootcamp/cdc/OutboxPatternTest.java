package io.cockroachdb.bootcamp.cdc;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.Chapter5Application;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {Chapter5Application.class})
public class OutboxPatternTest extends AbstractIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Order(1)
    @Test
    public void whenPlaceOneOrder_thenExpectOutboxEvent() {
        createCustomersAndProducts(10, 10);

        PurchaseOrder po = sampleDataService.withRandomCustomersAndProducts(10, 10,
                (customers, products) -> {
                    Assertions.assertFalse(customers.isEmpty(), "No customers");
                    Assertions.assertFalse(products.isEmpty(), "No products");

                    Product product = products.getFirst();

                    PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                            .withCustomer(customers.getFirst())
                            .andOrderItem()
                            .withProductId(product.getId())
                            .withProductSku(product.getSku())
                            .withUnitPrice(product.getPrice())
                            .withQuantity(1)
                            .then()
                            .build();

                    return orderService.placeOrder(UUID.randomUUID(), purchaseOrder);
                });
        Assertions.assertNotNull(po.getId());
    }
}
