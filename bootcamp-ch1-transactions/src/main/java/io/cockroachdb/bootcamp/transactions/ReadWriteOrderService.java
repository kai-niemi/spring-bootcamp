package io.cockroachdb.bootcamp.transactions;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;
import io.cockroachdb.bootcamp.util.AssertUtils;

@Service
public class ReadWriteOrderService extends AbstractOrderService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @TransactionExplicit
    @Retryable(predicate = TransientExceptionClassifier.class,
            maxRetries = 5,
            maxDelay = 15_0000,
            multiplier = 1.5)
    public void updateOrder(UUID id,
                            ShipmentStatus preCondition,
                            ShipmentStatus postCondition,
                            Simulation simulation) {
        AssertUtils.assertReadWriteTransaction();

        TypedQuery<PurchaseOrder> query = entityManager.createQuery(
                "select po from PurchaseOrder po where po.id=:id", PurchaseOrder.class);
        query.setParameter("id", id);
        query.setLockMode(simulation.getLockModeType());

        // SELECT .. FOR UPDATE
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

        entityManager.flush();
    }
}
