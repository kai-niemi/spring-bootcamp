package io.cockroachdb.bootcamp.hibernate;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An experimental statement interceptor that applies various transformations on
 * Hibernate generated SQL before passed to the DB. Interception is triggered by
 * semantic tokens detected in SQL hint comment prefixes. The comment itself names
 * the token and the transformation to apply on a SQL query, or many transformations
 * in a deterministic order.
 * <p>
 * This shows how elemental JPA/Hibernate SQL rewrites can be done to inject things like
 * optimizer join hints and other elements without changing any entity mappings or
 * JPQL queries.
 *
 * @author Kai Niemi
 */
public class QueryInterceptor implements StatementInspector {
    @FunctionalInterface
    private interface Transformation {
        String transform(String sql);
    }

    /**
     * Mapping semantic tokens to transformations.
     */
    private static final Map<String, Transformation> transformationMap
            = Map.of("replaceJoinWithInnerJoin",
            sql -> {
                return sql.replaceAll("join", "inner join");
            }, "appendForUpdate", sql -> {
                return sql + " FOR UPDATE";
            }, "appendForShare", sql -> {
                return sql + " FOR SHARE";
            });

    private static final Logger logger = LoggerFactory.getLogger(QueryInterceptor.class);

    private static final Pattern SQL_COMMENT = Pattern.compile("/\\*(.*?)\\*/\\s*");

    private static final Formatter FORMATTER_BASIC = FormatStyle.BASIC.getFormatter();

    private static final Formatter FORMATTER_HIGHLIGHT = FormatStyle.HIGHLIGHT.getFormatter();

    @Override
    public String inspect(final String sql) {
        Matcher m = SQL_COMMENT.matcher(sql);
        if (!m.find()) {
            return sql;
        }
        // Extract semantic token
        String comment = m.group(1).trim();

        // Strip the comment prefix
        final String sqlBefore = m.replaceAll("");
        String sqlAfter = sqlBefore;

        // Stack of transformations
        Deque<String> stack = new ArrayDeque<>();
        stack.push(sqlAfter);

        AtomicInteger transforms = new AtomicInteger();

        // Split by comma and transform in-order
        Arrays.stream(comment.split(","))
                .forEachOrdered(token -> {
                    if (transformationMap.containsKey(token)) {
                        String result = transformationMap.get(token).transform(stack.pop());
                        stack.push(result);
                        transforms.incrementAndGet();
                    }
                });

        // Last item is the final SQL
        sqlAfter = stack.pop();

        if (logger.isTraceEnabled() && transforms.get() > 0) {
            logger.trace("""
                    SQL rewrite with %d transforms
                    Before: [%s]
                     After: [%s]""".formatted(
                    transforms.get(),
                    FORMATTER_HIGHLIGHT.format(sqlBefore),
                    FORMATTER_HIGHLIGHT.format(sqlAfter)
            ));
        }

        return sqlAfter;
    }
}
