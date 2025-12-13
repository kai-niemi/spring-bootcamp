package io.cockroachdb.bootcamp.transactions.aspect;

import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.annotation.TransactionPriority;
import io.cockroachdb.bootcamp.aspect.AdvisorOrder;

/**
 * AOP aspect that sets specific and arbitrary transaction/session variables.
 * <p>
 * The main pre-condition is that there must be an existing transaction in scope.
 * This advice must be applied after the {@link TransactionRetryAspect} if used simultaneously,
 * and the Spring transaction advisor in the call chain.
 * <p>
 * See {@link org.springframework.transaction.annotation.EnableTransactionManagement} for
 * controlling weaving order.
 *
 * @author Kai Niemi
 */
@Aspect
@Order(TransactionAttributeAspect.PRECEDENCE)
public class TransactionAttributeAspect {
    static <A extends Annotation> A findAnnotation(ProceedingJoinPoint pjp, Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(pjp.getSignature().getDeclaringType(), annotationType);
    }

    /**
     * The precedence at which this advice is ordered by which also controls
     * the order it is invoked in the call chain between a source and target.
     */
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_CONTEXT_ADVISOR;

    private final JdbcTemplate jdbcTemplate;

    public TransactionAttributeAspect(DataSource dataSource) {
        Assert.notNull(dataSource, "dataSource is null");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Around(value = "io.cockroachdb.bootcamp.aspect.Pointcuts.anyExplicitTransactionBoundary(transactionExplicit)",
            argNames = "pjp,transactionExplicit")
    public Object doAroundTransactionalOperation(ProceedingJoinPoint pjp,
                                                 TransactionExplicit transactionExplicit)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting active transaction - check advice @Order and @EnableTransactionManagement order: "
                + pjp.getSignature().toShortString());

        // Grab from type if needed (for type-level annotations)
        if (transactionExplicit == null) {
            transactionExplicit = findAnnotation(pjp, TransactionExplicit.class);
        }

        Assert.notNull(transactionExplicit, "No @TransactionExplicit annotation found!?");

        if (!"(empty)".equals(transactionExplicit.applicationName())) {
            jdbcTemplate.update("SET application_name=?", transactionExplicit.applicationName());
        }

        if (!TransactionPriority.NORMAL.equals(transactionExplicit.priority())) {
            jdbcTemplate.update("SET TRANSACTION PRIORITY "
                                 + transactionExplicit.priority().name());
        } else {
            if (TransactionSynchronizationManager.hasResource(TransactionRetryAspect.RETRY_ASPECT_CALL_COUNT)) {
                Integer numCalls = (Integer) TransactionSynchronizationManager
                        .getResource(TransactionRetryAspect.RETRY_ASPECT_CALL_COUNT);
                // Increase priority on retry
                if (Objects.nonNull(numCalls) && numCalls > 1) {
                    jdbcTemplate.update("SET TRANSACTION PRIORITY "
                                         + transactionExplicit.retryPriority().name());
                }
            }
        }

        if (!"0s".equals(transactionExplicit.idleTimeout())) {
            jdbcTemplate.update("SET idle_in_transaction_session_timeout=?", transactionExplicit.idleTimeout());
        }

        if (transactionExplicit.readOnly()) {
            jdbcTemplate.update("SET transaction_read_only=true");
        }

        return pjp.proceed();
    }
}
