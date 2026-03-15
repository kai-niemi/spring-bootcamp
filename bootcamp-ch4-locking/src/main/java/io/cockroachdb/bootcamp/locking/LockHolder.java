package io.cockroachdb.bootcamp.locking;

import java.util.Objects;

public class LockHolder {
    private final Object lock;

    public LockHolder(Object lock) {
        Objects.requireNonNull(lock);
        this.lock = lock;
    }

    public <T> T getLockAs(Class<T> ofType) {
        return ofType.cast(lock);
    }
}
