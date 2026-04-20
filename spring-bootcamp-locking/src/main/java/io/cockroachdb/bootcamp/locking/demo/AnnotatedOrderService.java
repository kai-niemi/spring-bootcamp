package io.cockroachdb.bootcamp.locking.demo;

import java.util.Objects;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.locking.Singleton;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;

@Service
public class AnnotatedOrderService implements OrderService {
    private final Semaphore semaphore = new Semaphore(1);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @TransactionExplicit
    @Retryable(predicate = TransientExceptionClassifier.class,
            maxRetries = 5,
            maxDelay = 15_0000,
            multiplier = 1.5)
    @Singleton
    public PurchaseOrder placeOrder(PurchaseOrder order) {
        AssertUtils.assertReadWriteTransaction();

        Assert.state(semaphore.tryAcquire(), "Unable to acquire semaphore to assert singleton execution!");

        try {
            order.getOrderItems().forEach(orderItem -> {
                Product product = productRepository.getReferenceById(
                        Objects.requireNonNull(orderItem.getProduct().getId()));
                product.addInventoryQuantity(-orderItem.getQuantity());
            });

            order.setStatus(ShipmentStatus.placed);
            order.setTotalPrice(order.subTotal());

            orderRepository.saveAndFlush(order);
        } finally {
            semaphore.release();
        }

        return order;
    }
}
