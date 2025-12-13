package io.cockroachdb.bootcamp.cdc.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.annotation.Outbox;
import io.cockroachdb.bootcamp.aspect.AdvisorOrder;
import io.cockroachdb.bootcamp.cdc.outbox.OutboxRepository;

@Component
@Aspect
@Order(OutboxAspect.PRECEDENCE)
public class OutboxAspect {
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_AFTER_ADVISOR;

    private final OutboxRepository outboxRepository;

    public OutboxAspect(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    /**
     * Pointcut expression matching all outbox event operations.
     */
    @Pointcut("execution(public * *(..)) && @annotation(outbox)")
    public void anyOutboxOperation(Outbox outbox) {
    }

    @AfterReturning(pointcut = "anyOutboxOperation(outbox)",
            returning = "returnValue", argNames = "returnValue,outbox")
    public void doAfterOutboxOperation(Object returnValue, Outbox outbox) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expected existing transaction - check advisor @Order");

        outboxRepository.writeAggregate(
                outbox.aggregateClass().cast(returnValue),
                outbox.aggregateType());
    }
}

