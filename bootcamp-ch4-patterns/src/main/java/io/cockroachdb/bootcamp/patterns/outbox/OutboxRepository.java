package io.cockroachdb.bootcamp.patterns.outbox;

public interface OutboxRepository {
    void writeEvent(Object event, String aggregateType);
}
