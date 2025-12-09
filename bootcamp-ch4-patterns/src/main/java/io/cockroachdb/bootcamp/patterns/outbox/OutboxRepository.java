package io.cockroachdb.bootcamp.patterns.outbox;

import org.springframework.data.domain.Persistable;

public interface OutboxRepository {
    void writeAggregate(Persistable<?> event, String aggregateType);
}
