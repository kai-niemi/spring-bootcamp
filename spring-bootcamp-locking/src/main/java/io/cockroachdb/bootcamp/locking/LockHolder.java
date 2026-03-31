package io.cockroachdb.bootcamp.locking;

import java.util.Objects;

public class LockHolder {
    private final String name;

    private final Object lock;

    public LockHolder(String name, Object lock) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(lock);
        this.name = name;
        this.lock = lock;
    }


    public String getName() {
        return name;
    }

    public <T> T getLockAs(Class<T> ofType) {
        return ofType.cast(lock);
    }
}
