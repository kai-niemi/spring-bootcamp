package io.cockroachdb.bootcamp.locking;

public interface LockService {
    LockHolder acquireLock(LockContext lockContext);

    void releaseLock(LockHolder lock);
}
