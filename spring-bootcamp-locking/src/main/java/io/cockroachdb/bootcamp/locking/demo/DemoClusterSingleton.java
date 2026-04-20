package io.cockroachdb.bootcamp.locking.demo;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.locking.LockContext;
import io.cockroachdb.bootcamp.locking.LockHolder;
import io.cockroachdb.bootcamp.locking.LockService;

/**
 * A sample cluster singleton execution service, depending on lock service implementation.
 * It only allows the business method to run once at a time, at a cluster-wide / global scope
 * by leveraging an underpinning mutex mechanisms (typically database table(s) with built-in
 * lock semantics).
 *
 * @author Kai. Niemi
 * @see io.cockroachdb.bootcamp.locking.shedlock.ShedLockClusterSingleton
 */
@Service
@Profile(value = "!shedlock") // shedlock works differently so its got a separate implementation
public class DemoClusterSingleton {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Semaphore semaphore = new Semaphore(1);

    @Autowired
    private LockService lockService;

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    @TransactionExplicit
    public void checkInventory() {
        logger.info("Checking inventory - trying to acquire lock");

        Optional<LockHolder> lock = lockService.tryLock(LockContext.of("checkInventory"));
        if (lock.isPresent()) {
            Assert.state(semaphore.tryAcquire(), "Unable to acquire semaphore to assert singleton execution!");

            try {
                logger.info("Checking inventory - this is a heavy lift!");
                TimeUnit.SECONDS.sleep(10); // Sleep longer than scheduled interval
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                logger.info("Done checking inventory - releasing lock");
                semaphore.release();
                lockService.releaseLock(lock.get());
            }
        }
    }
}

