package io.cockroachdb.bootcamp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the annotated method is idempotent, thus safe to retry also due to
 * non serializability conflict related transient errors.
 *
 * @author Kai Niemi
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Idempotent {
    /**
     * Provides all SQL state codes that classifies a SQL error as transient and safe to retry. In the normal
     * case, that only includes 40001 that denotes a serialization conflict. All other state codes such as
     * dropped connections, rejected transactions etc. can still be safe to retry, but since the outcome of
     * the original transaction is indeterminate, safety depends on whether the SQL operations performed
     * were idempotent or not (like some UPDATE's or UPSERT's).
     *
     * @return array of transient SQL codes safe to retry
     */
    String[] transientSQLStates() default {
            "40001", "08001", "08003", "08004", "08006", "08007", "08S01", "57P01"
    };
}
