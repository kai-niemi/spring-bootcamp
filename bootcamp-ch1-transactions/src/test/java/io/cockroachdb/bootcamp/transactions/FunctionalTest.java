package io.cockroachdb.bootcamp.transactions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import io.cockroachdb.bootcamp.Chapter1Application;
import io.cockroachdb.bootcamp.model.Customer;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.PurchaseOrderItem;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;
import io.cockroachdb.bootcamp.repository.MetadataUtils;
import io.cockroachdb.bootcamp.test.AbstractIntegrationTest;

@SpringBootTest(classes = {Chapter1Application.class})
public class FunctionalTest extends AbstractIntegrationTest {
    @Autowired
    private DataSource dataSource;

    private UUID purchaseOrderId;

    @Autowired
    private OrderService orderService;

    @BeforeAll
    public void beforeAll() {
        String isolation = MetadataUtils.databaseIsolation(dataSource);
        Assertions.assertEquals("SERIALIZABLE", isolation.toUpperCase());

        createCustomersAndProducts(10, 10);
    }

    @Order(1)
    @Test
    public void whenPlaceOrder_thenExpectSuccess() {
        Page<Product> productPage = orderService.findProducts(PageRequest.ofSize(10));
        Page<Customer> customerPage = orderService.findCustomers(PageRequest.ofSize(10));

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

        Assertions.assertNotNull(result, "Expected detached entity");

        this.purchaseOrderId = result.getId();
    }

    @Order(3)
    @Test
    public void whenReadingOrder_thenExpectStatusUpdated() {
        Page<PurchaseOrder> orderPage = orderService.findOrders(PageRequest.ofSize(10));
        Assertions.assertEquals(1, orderPage.getTotalElements());

        PurchaseOrder purchaseOrder = orderPage.getContent().getFirst();
        Assertions.assertEquals(ShipmentStatus.placed, purchaseOrder.getStatus());
    }

    @Order(4)
    @Test
    public void whenReadingOrderLineProduct_thenExpectInventoryUpdated() {
        PurchaseOrder purchaseOrder = orderService.findOrderDetailById(purchaseOrderId).orElseThrow();

        List<PurchaseOrderItem> items = purchaseOrder.getOrderItems();
        Assertions.assertEquals(1, items.size());

        PurchaseOrderItem purchaseOrderItem = items.getFirst();
        Assertions.assertEquals(purchaseOrderItem.getUnitPrice()
                        .multiply(new BigDecimal(purchaseOrderItem.getQuantity())),
                purchaseOrder.getTotalPrice());

        Product product = purchaseOrderItem.getProduct();
        Assertions.assertEquals(0, product.getInventory());
    }

    @Order(5)
    @Test
    public void givenZeroInventory_whenPlaceOneOrder_thenExpectFailure() {
        Page<Product> productPage = orderService.findProducts(PageRequest.ofSize(10));
        Page<Customer> customerPage = orderService.findCustomers(PageRequest.ofSize(10));

        Assertions.assertFalse(customerPage.isEmpty(), "No customers");
        Assertions.assertFalse(productPage.isEmpty(), "No products");

        Product product = productPage.getContent().getFirst();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .withCustomer(customerPage.getContent().getFirst())
                .andOrderItem()
                .withProductId(product.getId())
                .withProductSku(product.getSku())
                .withUnitPrice(product.getPrice())
                .withQuantity(1)
                .then()
                .build();

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> {
            orderService.placeOrder(purchaseOrder);
        });
        Assertions.assertInstanceOf(DataIntegrityViolationException.class, ex.getCause());
    }

    @Order(6)
    @Test
    public void givenOrderStatusPlaced_whenUpdatingToConfirmed_thenExpectNewStatus() {
        orderService.updateOrder(purchaseOrderId, ShipmentStatus.placed, ShipmentStatus.confirmed,
                Simulation.none());

        PurchaseOrder purchaseOrder = orderService.findOrderById(purchaseOrderId).orElseThrow();
        Assertions.assertEquals(ShipmentStatus.confirmed, purchaseOrder.getStatus());
    }

    @Order(7)
    @Test
    public void givenOrderStatusConfirmed_whenUpdatingToDelivered_thenExpectNewStatus() {
        orderService.updateOrder(purchaseOrderId, ShipmentStatus.confirmed, ShipmentStatus.delivered,
                Simulation.none());

        PurchaseOrder purchaseOrder = orderService.findOrderById(purchaseOrderId).orElseThrow();
        Assertions.assertEquals(ShipmentStatus.delivered, purchaseOrder.getStatus());
    }

    @Order(8)
    @Test
    public void givenQueryTransformation_whenListingAllOrderDetails_thenExpectJoinHints() {
        orderService.findOrderDetails().forEach(this::print);
    }

    private void print(PurchaseOrder order) {
        Customer c = order.getCustomer();
        logger.info("""
                Order placed by: %s
                     Total cost: %s
                """.formatted(c.getEmail(), order.getTotalPrice()));
    }

    private void printDetails(PurchaseOrder order) {
        Customer c = order.getCustomer();

        logger.info("""
                Order placed by: %s
                     Total cost: %s
                """.formatted(c.getEmail(), order.getTotalPrice()));

        order.getOrderItems().forEach(orderItem -> {
            Product p = orderItem.getProduct();

            logger.info("""
                     Product name: %s
                    Product price: %s
                      Product sku: %s
                         Item qty: %s
                       Unit price: %s
                       Total cost: %s
                    """.formatted(
                    p.getName(),
                    p.getPrice(),
                    p.getSku(),
                    orderItem.getQuantity(),
                    orderItem.getUnitPrice(),
                    orderItem.totalCost()));
        });
    }
}


