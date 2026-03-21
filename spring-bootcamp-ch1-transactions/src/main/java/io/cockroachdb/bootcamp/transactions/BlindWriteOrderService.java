package io.cockroachdb.bootcamp.transactions;

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

@Service
public class BlindWriteOrderService extends AbstractOrderService {
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


        // UPDATE directly, which is also a SELECT .. FOR UPDATE in the read part
        Query update = entityManager.createQuery(
                "update PurchaseOrder po set po.status=:postStatus where po.id=:id and po.status=:preStatus");
        update.setParameter("id", id);
        update.setParameter("preStatus", preCondition);
        update.setParameter("postStatus", postCondition);
        update.setLockMode(LockModeType.NONE);

        int rowsAffected = update.executeUpdate();
        if (rowsAffected != 1) {
            logger.warn("Precondition failed (no-op): %s != %s".formatted(preCondition, postCondition));
        }

        simulation.thinkTime();

        entityManager.flush();
    }
}

