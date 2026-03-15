# Chapter 4 :: Locking pattern

Module for demonstrating different cluster singleton and mutex locking techniques equivalent 
to PG advisory locks.

Demonstrated alternatives:

| Alternative | Description                                                                                                                                 | Mutex 1) | Cluster Singleton 2) |
|-------------|---------------------------------------------------------------------------------------------------------------------------------------------|----------|----------------------|
| JDBC        | An equivalent to PG advisory locks using provisional write intents through UPSERTs in explicit transactions.                                | Yes      | Yes                  |
| In-mem      | A JVM local reentrant lock in case none of the above is used.                                                                               | Yes      | No                   |
| Shedlock    | A cluster singleton lock service for scheduled tasks. Note that it cannot be used as a mutex and it depends on timeouts which can be risky. | No       | Yes                  |

1) Provides mutual exclusion locks for arbitrary business service methods.
   Methods can only execute in a sequential order in a JVM scope where 
   the sequence order is determined by lock acquisition local time.
2) Provides cluster singleton execution for scheduled methods at global scale.
   Methods can only execute in a sequential order across multiple, independent 
   JVMs where the sequence order is determined by transaction timestamp (that have a HLC 
   component in CRDB). 

See also:

- [Time and hybrid logical clocks](https://www.cockroachlabs.com/docs/stable/architecture/transaction-layer#time-and-hybrid-logical-clocks)
- [Shedlock](https://github.com/lukas-krecan/ShedLock)
- [Using Shedlock with CockroachDB](https://blog.cloudneutral.se/cluster-singletons-using-shedlock)

## Running Tests

This script will present a menu of available tests to trigger the workflows:

    ./run-test.sh
