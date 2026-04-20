package io.cockroachdb.bootcamp.locking;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import io.cockroachdb.bootcamp.aspect.AdvisorOrder;
import io.cockroachdb.bootcamp.util.AssertUtils;

@Aspect
@Order(LockAspect.PRECEDENCE)
public class LockAspect {
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_CONTEXT_ADVISOR;

    private final LockService lockService;

    public LockAspect(@Autowired LockService lockService) {
        this.lockService = lockService;
    }

    /**
     * Pointcut expression matching all idempotent operations.
     */
    @Pointcut("execution(public * *(..)) && @annotation(singleton)")
    public void anySingletonOperation(Singleton singleton) {
    }

    @Around(value = "anySingletonOperation(singleton)",
            argNames = "pjp,singleton")
    public Object doAroundSingletonOperation(ProceedingJoinPoint pjp, Singleton singleton)
            throws Throwable {
        AssertUtils.assertReadWriteTransaction();

        LockHolder lockHolder = lockService.acquireLock(
                LockContext.of(pjp.getSignature().toShortString()));
        try {
            return pjp.proceed();
        } finally {
            lockService.releaseLock(lockHolder);
        }
    }
}
