package io.cockroachdb.bootcamp.patterns.aspect;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.annotation.Idempotent;
import io.cockroachdb.bootcamp.aspect.AdvisorOrder;
import io.cockroachdb.bootcamp.model.IdempotencyException;
import io.cockroachdb.bootcamp.model.IdempotencyKeyHolder;
import io.cockroachdb.bootcamp.model.IdempotencyToken;
import io.cockroachdb.bootcamp.repository.IdempotencyTokenRepository;

@Aspect
@Order(IdempotencyAspect.PRECEDENCE)
public class IdempotencyAspect {
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_CONTEXT_ADVISOR;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IdempotencyTokenRepository idempotencyTokenRepository;

    public IdempotencyAspect(IdempotencyTokenRepository idempotencyTokenRepository) {
        this.idempotencyTokenRepository = idempotencyTokenRepository;
    }

    /**
     * Pointcut expression matching all idempotent operations.
     */
    @Pointcut("execution(public * *(..)) && @annotation(idempotent)")
    public void anyIdempotentOperation(Idempotent idempotent) {
    }

    @Around(value = "anyIdempotentOperation(idempotent)",
            argNames = "pjp,idempotent")
    public Object doAroundIdempotentOperation(ProceedingJoinPoint pjp, Idempotent idempotent)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting active transaction - check advice @Order and @EnableTransactionManagement order: "
                + pjp.getSignature().toShortString());

        // Inspect first method arg
        Optional<UUID> id = Arrays.stream(pjp.getArgs())
                .filter(o -> o instanceof UUID)
                .map(o -> (UUID) o)
                .findFirst();

        // Otherwise inspect all args
        Optional<UUID> idempotencyKey = id.isPresent() ? id :
                Arrays.stream(pjp.getArgs())
                        .filter(o -> o instanceof IdempotencyKeyHolder)
                        .map(o -> (IdempotencyKeyHolder) o)
                        .map(IdempotencyKeyHolder::resolveKey)
                        .filter(Objects::nonNull)
                        .findFirst();

        if (idempotencyKey.isPresent()) {
            Optional<IdempotencyToken> existingToken = idempotencyTokenRepository
                    .findByIdempotencyKey(idempotencyKey.get());
            if (existingToken.isPresent()) {
                logger.debug("Found idempotency token for key '%s' for '%s'"
                        .formatted(idempotencyKey, pjp.toShortString()));
                return null;
            }
        }

        Object rv = pjp.proceed();

        if (rv instanceof IdempotencyKeyHolder) {
            UUID key = ((IdempotencyKeyHolder) rv).resolveKey();
            Objects.requireNonNull(key, "Idempotency key is null for %s".formatted(pjp.toShortString()));

            idempotencyTokenRepository.save(new IdempotencyToken(key));

            logger.debug("Add idempotency token for key '%s' for '%s'"
                    .formatted(idempotencyKey, pjp.toShortString()));
        } else {
            throw new IdempotencyException("Unable to resolve idempotency key from method signature '%s'"
                    .formatted(pjp.toShortString()));
        }

        return rv;
    }
}

