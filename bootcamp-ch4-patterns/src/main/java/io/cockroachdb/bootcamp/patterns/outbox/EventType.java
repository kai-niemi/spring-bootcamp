package io.cockroachdb.bootcamp.patterns.outbox;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EventType {
    insert,
    update,
    upsert,
    delete
}
