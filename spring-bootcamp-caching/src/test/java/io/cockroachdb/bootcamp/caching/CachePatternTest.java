package io.cockroachdb.bootcamp.caching;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import io.cockroachdb.bootcamp.CacheApplication;
import io.cockroachdb.bootcamp.model.Customer;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.PurchaseOrderItem;
import io.cockroachdb.bootcamp.repository.CustomerRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {CacheApplication.class})
public class CachePatternTest extends AbstractIntegrationTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderService orderService;

    private UUID purchaseOrderId;

    @BeforeAll
    public void beforeAll() {
        createCatalog(10, 10);
    }

    @Order(1)
    @Test
    public void whenPlaceOrder_thenExpectSingleOrder() {
        Page<Product> productPage = productRepository.findAll(PageRequest.ofSize(10));
        Page<Customer> customerPage = customerRepository.findAll(PageRequest.ofSize(10));

        Assertions.assertFalse(customerPage.isEmpty(), "No customers");
        Assertions.assertFalse(productPage.isEmpty(), "No products");

        Product product = productPage.getContent().getFirst();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .withCustomer(customerPage.getContent().getFirst())
                .andOrderItem()
                .withProductId(product.getId())
                .withProductSku(product.getSku())
                .withUnitPrice(product.getPrice())
                .withQuantity(product.getInventory())
                .then()
                .build();

        PurchaseOrder result = orderService.placeOrder(purchaseOrder);
        this.purchaseOrderId = result.getId();
    }

    @Order(2)
    @Test
    public void whenReadingOrderById_givenCacheMiss_thenExpectDelay() {
        final Instant start = Instant.now();

        PurchaseOrder purchaseOrder = orderService.findOrderById(purchaseOrderId)
                .orElseThrow();

        Assertions.assertTrue(
                Duration.between(start, Instant.now()).toMillis() >= 5_000,
                "Op should take at least 5s");

        List<PurchaseOrderItem> items = purchaseOrder.getOrderItems();
        Assertions.assertEquals(1, items.size());
    }

    @Order(3)
    @Test
    public void whenReadingOrderById_givenCacheHit_thenExpectNoDelay() {
        final Instant start = Instant.now();

        PurchaseOrder purchaseOrder = orderService.findOrderById(purchaseOrderId)
                .orElseThrow();

        Assertions.assertTrue(
                Duration.between(start, Instant.now()).toMillis() < 5_000,
                "Op should take strictly less than 5s");

        List<PurchaseOrderItem> items = purchaseOrder.getOrderItems();
        Assertions.assertEquals(1, items.size());
    }

    @Order(4)
    @Test
    public void whenDeletingOrderById_thenExpectCacheEvict() {
        orderService.deleteOrder(purchaseOrderId);
    }

    @Order(5)
    @Test
    public void whenReadingOrderById_givenCacheMissAgain_thenExpectDelay() {
        final Instant start = Instant.now();

        PurchaseOrder purchaseOrder = orderService.findOrderById(purchaseOrderId)
                .orElseThrow();

        Assertions.assertTrue(
                Duration.between(start, Instant.now()).toMillis() >= 5_000,
                "Op should take at least 5s");
        List<PurchaseOrderItem> items = purchaseOrder.getOrderItems();
        Assertions.assertEquals(1, items.size());
    }
}


