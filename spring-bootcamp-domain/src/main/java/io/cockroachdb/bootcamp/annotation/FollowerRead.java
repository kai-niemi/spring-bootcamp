package io.cockroachdb.bootcamp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class or method can read from a given timestamp in the past.
 * Follower reads in CockroachDB represents a computed time interval sufficiently in the past
 * for reads to be served by closest follower replica.
 *
 * @author Kai Niemi
 * @see <a href="https://www.cockroachlabs.com/docs/stable/follower-reads">Follower Reads</a>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface FollowerRead {
    /**
     * @return the follower read type
     */
    FollowerReadType type() default FollowerReadType.EXACT_STALENESS_READ;

    /**
     * @return interval expression that translates to follower_read_timestamp() if '0s' for EXACT_STALENESS_READ
     * or maximum staleness interval for BOUNDED_STALENESS_READ
     * @see <a href="https://www.cockroachlabs.com/docs/stable/interval.html">Interval</a>
     */
    String interval() default "0s";
}
