package io.cockroachdb.bootcamp.locking;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * A no-op lock service that doesnt do any locking at all to demo that things fail.
 */
@Service
@Profile("nolock")
public class NoLockService implements LockService {
    @Override
    public LockHolder acquireLock(LockContext lockContext) {
        return new LockHolder(lockContext.getName());
    }

    @Override
    public void releaseLock(LockHolder lock) {

    }
}
