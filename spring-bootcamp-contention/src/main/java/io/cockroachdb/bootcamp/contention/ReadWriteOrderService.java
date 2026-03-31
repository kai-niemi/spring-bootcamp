package io.cockroachdb.bootcamp.contention;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;
import io.cockroachdb.bootcamp.util.AssertUtils;

/**
 * Business service facade for the order system. This service represents the
 * transaction boundary and gateway to all business functionality such as order
 * placement.
 */
@Service
public class ReadWriteOrderService extends AbstractOrderService {
    @PersistenceContext
    private EntityManager em;

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
