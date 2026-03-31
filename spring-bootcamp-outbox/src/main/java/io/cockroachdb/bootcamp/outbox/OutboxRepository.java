package io.cockroachdb.bootcamp.outbox;

import org.springframework.data.domain.Persistable;

public interface OutboxRepository {
    void writeAggregate(Persistable<?> event, String aggregateType);
}
