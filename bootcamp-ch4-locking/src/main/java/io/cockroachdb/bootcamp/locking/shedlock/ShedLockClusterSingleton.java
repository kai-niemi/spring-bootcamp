package io.cockroachdb.bootcamp.locking.shedlock;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;

@Service
@Profile(value = "shedlock")
public class ShedLockClusterSingleton {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Semaphore semaphore = new Semaphore(1);

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(lockAtLeastFor = "0", lockAtMostFor = "2m", name = "checkInventory")
    @TransactionExplicit
    public void checkInventory() {
        LockAssert.assertLocked();

        logger.info("Checking inventory - acquired lock");

        Assert.state(semaphore.tryAcquire(), "Unable to acquire semaphore to assert singleton execution!");

        try {
            logger.info("Checking inventory - this is a heavy lift!");
            TimeUnit.SECONDS.sleep(10); // Sleep longer than scheduled interval
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.info("Done checking inventory - releasing lock");
            semaphore.release();
        }
    }
}
