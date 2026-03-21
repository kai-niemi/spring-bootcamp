package io.cockroachdb.bootcamp.inbox;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.InboxApplication;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {InboxApplication.class})
public class InboxPatternTest extends AbstractIntegrationTest {
    @Autowired
    private InboxRepository<PurchaseOrder> inboxRepository;

    @Order(1)
    @Test
    public void whenPlaceOneOrder_thenExpectInboxEvent() {
        createCatalog(10, 10);

        dataService.withRandomCustomersAndProducts(10, 10,
                (customers, products) -> {
                    Assertions.assertFalse(customers.isEmpty(), "No customers");
                    Assertions.assertFalse(products.isEmpty(), "No products");

                    Product product = products.getFirst();

                    PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                            .withGeneratedId()
                            .withCustomer(customers.getFirst())
                            .andOrderItem()
                            .withProductId(product.getId())
                            .withProductSku(product.getSku())
                            .withUnitPrice(product.getPrice())
                            .withQuantity(1)
                            .then()
                            .build();

                    inboxRepository.writeAggregate(purchaseOrder, "purchase_order");

                    return purchaseOrder;
                });
    }
}
