package io.cockroachdb.bootcamp.patterns.event;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EventType {
    insert,
    update,
    upsert,
    delete
}
