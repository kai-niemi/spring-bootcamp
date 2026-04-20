package io.cockroachdb.bootcamp.interceptor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;

@Service
public class DefaultOrderService implements OrderService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    @TransactionExplicit(readOnly = true)
    public Optional<PurchaseOrder> findOrderById(UUID id) {
        AssertUtils.assertReadOnlyTransaction();
        return orderRepository.findById(id);
    }

    /**
     * Place a single purchase order.
     *
     * @param order the order detail in detached state
     */
    @Override
    @TransactionExplicit
    public PurchaseOrder placeOrder(PurchaseOrder order) throws BusinessException {
        AssertUtils.assertReadWriteTransaction();

        try {
            // Update product inventories for each line item
            order.getOrderItems().forEach(orderItem -> {
                UUID productId = Objects.requireNonNull(orderItem.getProduct().getId());
                Product product = productRepository.getReferenceById(productId);
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

    @Override
    @TransactionExplicit
    @Retryable(predicate = TransientExceptionClassifier.class,
            maxRetries = 5,
            maxDelay = 15_0000,
            multiplier = 1.5)
    public void updateOrder(UUID id, ShipmentStatus preCondition, ShipmentStatus postCondition,
                            Simulation simulation) {
        AssertUtils.assertReadWriteTransaction();

        TypedQuery<PurchaseOrder> query = em.createNamedQuery("findOrderById", PurchaseOrder.class);
        query.setParameter("id", id);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setLockMode(simulation.getLockModeType());

        PurchaseOrder purchaseOrder = query.getSingleResult();
        if (purchaseOrder == null) {
            throw new ObjectRetrievalFailureException(PurchaseOrder.class, id);
        }

        if (purchaseOrder.getStatus().equals(preCondition)) {
            purchaseOrder.setStatus(postCondition);
            purchaseOrder.setDateUpdated(LocalDateTime.now());
        } else {
            logger.warn("Precondition failed (no-op): %s != %s".formatted(preCondition, postCondition));
        }
        simulation.thinkTime();

        em.flush();
    }
}

