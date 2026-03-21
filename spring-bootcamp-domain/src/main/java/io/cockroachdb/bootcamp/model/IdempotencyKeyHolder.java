package io.cockroachdb.bootcamp.model;

import java.util.UUID;

@FunctionalInterface
public interface IdempotencyKeyHolder {
    UUID resolveKey();
}
