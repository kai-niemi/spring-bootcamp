package io.cockroachdb.bootcamp.patterns;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.Chapter4Application;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;
import io.cockroachdb.bootcamp.patterns.inbox.InboxService;

@SpringBootTest(classes = {Chapter4Application.class})
public class InboxPatternTest extends AbstractIntegrationTest {
    @Autowired
    private InboxService inboxService;

    @Order(1)
    @Test
    public void whenPlaceOneOrder_thenExpectInboxEvent() {
        createCustomersAndProducts(10, 10);

        sampleDataService.withRandomCustomersAndProducts(10, 10,
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

                    return inboxService.submitPurchaseOrder(purchaseOrder);
                });
    }
}
