package io.cockroachdb.bootcamp.locking.demo;

import java.util.Objects;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.locking.LockContext;
import io.cockroachdb.bootcamp.locking.LockHolder;
import io.cockroachdb.bootcamp.locking.LockService;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;

//@Service
public class ProgrammaticOrderService implements OrderService {
    private final Semaphore semaphore = new Semaphore(1);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LockService lockService;

    @Override
    @TransactionExplicit
    @Retryable(predicate = TransientExceptionClassifier.class,
            maxRetries = 5,
            maxDelay = 15_0000,
            multiplier = 1.5)
    public PurchaseOrder placeOrder(PurchaseOrder order) {
        AssertUtils.assertReadWriteTransaction();

        LockHolder lock = lockService.acquireLock(LockContext.of("placeOrder"));

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
            lockService.releaseLock(lock);
        }

        return order;
    }
}
