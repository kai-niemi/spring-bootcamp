package io.cockroachdb.bootcamp.transactions;

import org.springframework.context.annotation.Profile;

@Profile({"default", "aspect-retry"})
public class AspectRetryTest extends AbstractRetryTest {
}
