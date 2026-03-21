package io.cockroachdb.bootcamp.idempotency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.IdempotencyApplication;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {IdempotencyApplication.class})
public class IdempotencyPatternTest extends AbstractIntegrationTest {
    @Autowired
    private OrderService orderService;

    private PurchaseOrder purchaseOrder;

    @BeforeAll
    public void beforeAll() {
        createCatalog(10, 10);
    }

    @Order(1)
    @Test
    public void whenPlaceOrder_thenExpectSuccess() {
        this.purchaseOrder = dataService.withRandomCustomersAndProducts(10, 10,
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
        orderService.placeOrder(purchaseOrder.getId(), purchaseOrder);
        Assertions.assertNotNull(purchaseOrder, "Expected detached entity");
    }

    @Order(2)
    @Test
    public void whenPlaceOrderAgain_thenExpectDeDuplication() {
        PurchaseOrder secondOrder = dataService.withRandomCustomersAndProducts(10, 10,
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
        secondOrder = orderService.placeOrder(purchaseOrder.getId(), secondOrder);
        Assertions.assertNull(secondOrder, "Expected de-duplication");
    }
}
