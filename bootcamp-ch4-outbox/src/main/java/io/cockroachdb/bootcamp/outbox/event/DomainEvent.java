package io.cockroachdb.bootcamp.outbox.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A generic inbox/outbox domain event wrapper for CockroachDB CDC
 * queries with 'bare' envelopes.
 *
 * @param <T> the payload generic type (json serde)
 */
public abstract class DomainEvent<T> {
    // Correlates with the Kafka event key.
    // Projection into payload 'id' attribute.
    @JsonProperty("aggregate_id")
    private UUID aggregateId;

    // When using diff change feeds, provides the CRUD operation type
    @JsonProperty("event_type")
    private EventType eventType;

    protected T payload;

    protected DomainEvent() {
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
               "eventType=" + eventType +
               ", id=" + aggregateId +
               ", payload=" + payload +
               '}';
    }
}
