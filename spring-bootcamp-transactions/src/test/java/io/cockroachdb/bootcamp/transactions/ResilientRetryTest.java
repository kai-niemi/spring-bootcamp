package io.cockroachdb.bootcamp.transactions;

import org.springframework.context.annotation.Profile;

@Profile({"default", "resilient-retry"})
public class ResilientRetryTest extends AbstractRetryTest {
}
