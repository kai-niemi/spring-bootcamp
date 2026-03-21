package io.cockroachdb.bootcamp.batching;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.annotation.FollowerRead;
import io.cockroachdb.bootcamp.aspect.AdvisorOrder;

@Aspect
@Order(FollowerReadAspect.PRECEDENCE)
public class FollowerReadAspect {
    /**
     * The precedence at which this advice is ordered by which also controls
     * the order it is invoked in the call chain between a source and target.
     */
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_CONTEXT_ADVISOR;

    private final JdbcTemplate jdbcTemplate;

    public FollowerReadAspect(DataSource dataSource) {
        Assert.notNull(dataSource, "dataSource is null");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Pointcut("execution(public * *(..)) && @annotation(followerRead)")
    public void anyFollowerReadOperation(FollowerRead followerRead) {
    }

    @Around(value = "anyFollowerReadOperation(followerRead)", argNames = "pjp,followerRead")
    public Object doAroundTransactionalOperation(ProceedingJoinPoint pjp, FollowerRead followerRead)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting active transaction - check advice @Order and @EnableTransactionManagement order: "
                + pjp.getSignature().toShortString());
        Assert.isTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly(),
                "Expecting read-only transaction - check @Transactional readonly attribute: "
                + pjp.getSignature().toShortString());

        if (followerRead.type().equals(io.cockroachdb.bootcamp.annotation.FollowerReadType.EXACT_STALENESS_READ)) {
            if ("0s".equals(followerRead.interval())) {
                jdbcTemplate.execute("SET TRANSACTION AS OF SYSTEM TIME follower_read_timestamp()");
            } else {
                jdbcTemplate.update("SET TRANSACTION AS OF SYSTEM TIME INTERVAL '"
                                    + followerRead.interval() + "'");
            }
        } else if (followerRead.type().equals(io.cockroachdb.bootcamp.annotation.FollowerReadType.BOUNDED_STALENESS_READ)) {
            jdbcTemplate.update("SET TRANSACTION AS OF SYSTEM TIME with_max_staleness('"
                                + followerRead.interval() + "')");
        } else {
            throw new UnsupportedOperationException("Not a supported followerRead type");
        }

        return pjp.proceed();
    }
}
