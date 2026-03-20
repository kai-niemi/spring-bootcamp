package io.cockroachdb.bootcamp.contention;

import java.util.UUID;

import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;
import io.cockroachdb.bootcamp.util.AssertUtils;

/**
 * Business service facade for the order system. This service represents the
 * transaction boundary and gateway to all business functionality such as order
 * placement.
 */
@Service
public class BlindWriteOrderService extends AbstractOrderService {
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

        Query update = em.createNamedQuery("updateOrderStatusById");
        update.setParameter("id", id);
        update.setParameter("preStatus", preCondition);
        update.setParameter("postStatus", postCondition);
        update.setLockMode(LockModeType.NONE);

        int rowsAffected = update.executeUpdate();
        if (rowsAffected != 1) {
            logger.warn("Precondition failed (no-op): %s != %s".formatted(preCondition, postCondition));
        }

        simulation.thinkTime();

        em.flush();
    }
}
