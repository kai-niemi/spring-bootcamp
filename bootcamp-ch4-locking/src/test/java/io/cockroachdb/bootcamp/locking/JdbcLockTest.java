package io.cockroachdb.bootcamp.locking;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"default", "verbose", "jdbclock"})
public class JdbcLockTest extends LockingPatternTest {
}
