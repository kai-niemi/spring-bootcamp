package io.cockroachdb.bootcamp.locking;

import java.util.Objects;

public class LockContext {
    public static LockContext of(String name) {
        return new LockContext(name);
    }

    private final String name;

    public LockContext(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
