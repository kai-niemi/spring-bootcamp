package io.cockroachdb.bootcamp.patterns.inbox;

public interface InboxRepository<T> {
    void writeAggregate(T event, String aggregateType);
}
