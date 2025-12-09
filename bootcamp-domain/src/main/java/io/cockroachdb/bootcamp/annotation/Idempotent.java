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
    String[] transientSQLStates() default {
            "08001", "08003", "08004", "08006", "08007", "08S01", "57P01"
    };
}
