package io.cockroachdb.bootcamp.interceptor;

import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.cockroachdb.bootcamp.InterceptorApplication;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {InterceptorApplication.class})
public abstract class AbstractInterceptorTest extends AbstractIntegrationTest {
    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected OrderService orderService;

    protected UUID purchaseOrderId1;

    protected UUID purchaseOrderId2;

    @BeforeAll
    public void beforeAll() {
        createCatalog(10, 10);

        this.purchaseOrderId1 = placeOrder();
        this.purchaseOrderId2 = placeOrder();
    }

    private UUID placeOrder() {
        return dataService.withRandomCustomersAndProducts(100, 100,
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

                    return orderService.placeOrder(purchaseOrder).getId();
                });
    }
}
