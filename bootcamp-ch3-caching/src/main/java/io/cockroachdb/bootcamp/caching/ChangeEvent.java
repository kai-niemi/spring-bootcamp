package io.cockroachdb.bootcamp.caching;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object for change feed options 'envelope=enriched, key_in_value',
 */
public class ChangeEvent {
    public enum Operation {
        insert,
        update,
        delete,
        undefined
    }

    @JsonProperty("key")
    private Map<String, Object> key = new LinkedHashMap<>();

    @JsonProperty("after")
    private Map<String, Object> after = new LinkedHashMap<>();

    @JsonProperty("op")
    private String op;

    @JsonProperty("ts_ns")
    private String timestamp;

    public Map<String, Object> getKey() {
        return key;
    }

    public Map<String, Object> getAfter() {
        return after;
    }

    public Instant getTimestamp() {
        long nanos = TimeUnit.NANOSECONDS.toNanos(Long.parseLong(timestamp));
        long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
        return Instant.ofEpochMilli(millis);
    }

    public Operation getOperation() {
        if ("i".equalsIgnoreCase(op)) {
            return Operation.insert;
        }
        if ("u".equalsIgnoreCase(op)) {
            return Operation.update;
        }
        if ("d".equalsIgnoreCase(op)) {
            return Operation.delete;
        }
        return Operation.undefined;
    }
}

