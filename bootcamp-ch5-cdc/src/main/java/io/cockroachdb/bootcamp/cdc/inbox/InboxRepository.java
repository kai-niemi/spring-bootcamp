package io.cockroachdb.bootcamp.cdc.inbox;

public interface InboxRepository<T> {
    void writeAggregate(T event, String aggregateType);
}
