package io.cockroachdb.bootcamp.annotation;

/**
 * Enumeration of follower read types.
 * <p>
 * See {@link <a href="https://www.cockroachlabs.com/docs/stable/as-of-system-time.html">AS OF SYSTEM TIME</a>}
 *
 * @author Kai Niemi
 */
public enum FollowerReadType {
    /**
     * A historical read as of a static, user-provided timestamp.
     */
    EXACT_STALENESS_READ,
    /**
     * A historical read that uses a dynamic, system-determined timestamp to minimize staleness
     * while being more tolerant to replication lag than an exact staleness read.
     */
    BOUNDED_STALENESS_READ
}
