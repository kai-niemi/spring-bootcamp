package io.cockroachdb.bootcamp.locking;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;

/**
 * @author Kai Niemi
 */
@Service
public class DefaultOrderService implements OrderService {
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
    public PurchaseOrder placeOrder(UUID idempotencyKey, PurchaseOrder order) {
        AssertUtils.assertReadWriteTransaction();

        // Update product inventories for each line item
        order.getOrderItems().forEach(orderItem -> {
            Product product = productRepository.getReferenceById(
                    Objects.requireNonNull(orderItem.getProduct().getId()));
            product.addInventoryQuantity(-orderItem.getQuantity());
        });

        order.setStatus(ShipmentStatus.placed);
        order.setTotalPrice(order.subTotal());

        orderRepository.saveAndFlush(order); // flush to surface any constraint violations

        return order;
    }
}
