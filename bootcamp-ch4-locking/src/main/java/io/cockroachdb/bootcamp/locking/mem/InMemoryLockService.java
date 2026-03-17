package io.cockroachdb.bootcamp.locking.mem;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.cockroachdb.bootcamp.locking.LockContext;
import io.cockroachdb.bootcamp.locking.LockHolder;
import io.cockroachdb.bootcamp.locking.LockService;

/**
 * A simple in-memory reentrant lock service.
 *
 * @author Kai Niemi
 */
@Service
@Profile("memlock")
public class InMemoryLockService implements LockService {
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();

    @Override
    public LockHolder acquireLock(LockContext lockContext) {
        Lock lock = locks.computeIfAbsent(lockContext.getName(), s -> new ReentrantLock());
        lock.lock();
        return new LockHolder(lockContext.getName(), lock);
    }

    @Override
    public Optional<LockHolder> tryLock(LockContext lockContext) {
        Lock lock = locks.computeIfAbsent(lockContext.getName(), s -> new ReentrantLock());
        if (lock.tryLock()) {
            return Optional.of(new LockHolder(lockContext.getName(), lock));
        }
        return Optional.empty();
    }

    @Override
    public void releaseLock(LockHolder lock) {
        lock.getLockAs(Lock.class).unlock();
    }
}
