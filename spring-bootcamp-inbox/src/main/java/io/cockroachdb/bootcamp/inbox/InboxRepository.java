package io.cockroachdb.bootcamp.inbox;

public interface InboxRepository<T> {
    void writeAggregate(T event, String aggregateType);
}
