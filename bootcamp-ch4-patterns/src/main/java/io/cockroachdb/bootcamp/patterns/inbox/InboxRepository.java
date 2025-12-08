package io.cockroachdb.bootcamp.patterns.inbox;

public interface InboxRepository {
    void writeEvent(Object event, String aggregateType);
}
