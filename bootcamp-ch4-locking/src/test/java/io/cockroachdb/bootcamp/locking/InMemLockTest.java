package io.cockroachdb.bootcamp.locking;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"default","verbose","memlock"})
public class InMemLockTest  extends LockingPatternTest {
}
