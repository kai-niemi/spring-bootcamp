package io.cockroachdb.bootcamp.patterns.outbox;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.cockroachdb.bootcamp.annotation.ServiceFacade;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;
import io.cockroachdb.bootcamp.patterns.BusinessException;

@ServiceFacade
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class OutboxOrderService implements OrderService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(predicate = TransientExceptionClassifier.class,
            maxRetries = 5,
            maxDelay = 15_0000,
            multiplier = 1.5)
    @OutboxOperation(aggregateType = "purchase_order")
    public PurchaseOrder placeOrder(PurchaseOrder order) throws BusinessException {
        AssertUtils.assertReadWriteTransaction();

        try {
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
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Constraint violation", e);
        }
    }
}
