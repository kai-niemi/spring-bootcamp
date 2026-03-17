package io.cockroachdb.bootcamp.locking;

import java.util.Optional;

public interface LockService {
    LockHolder acquireLock(LockContext lockContext);

    Optional<LockHolder> tryLock(LockContext lockContext);

    void releaseLock(LockHolder lock);
}
