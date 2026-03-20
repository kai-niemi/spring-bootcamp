package io.cockroachdb.bootcamp.transactions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.TransactionApplication;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {TransactionApplication.class})
public class TransactionLifetimeTest extends AbstractIntegrationTest {
    @Autowired
    @Qualifier("readWriteOrderService")
    private OrderService orderService;

    @Order(1)
    @Test
    public void whenPlaceOrderWithForeignServiceValidation_thenExpectShortLivedTransaction() {
        PurchaseOrder purchaseOrder = sampleDataService.withRandomCustomersAndProducts(100, 100,
                (customers, products) -> {
                    Assertions.assertFalse(customers.isEmpty(), "No customers");
                    Assertions.assertFalse(products.isEmpty(), "No products");

                    Product product = products.getFirst();

                    return PurchaseOrder.builder()
                            .withCustomer(customers.getFirst())
                            .andOrderItem()
                            .withProductId(product.getId())
                            .withProductSku(product.getSku())
                            .withUnitPrice(product.getPrice())
                            .withQuantity(1)
                            .then()
                            .build();
                });
        orderService.placeOrderWithLongWait(purchaseOrder);
    }
}
