# Chapter 4 :: Patterns

Module for demonstrating various design patterns, mainly the transactional inbox and outbox.

Key topics:

- Inbox pattern via CDC to Kafka
- Outbox pattern via CDC to Kafka

## Prerequisites

Kafka is required to demonstrate CockroachDB built-in CDC feature. You can either use a manged Kafka cluster 
or a local self-hosted setup. In the latter case, just follow the [quickstart](https://kafka.apache.org/quickstart) guidelines
to setup a vanilla Kafka instance.
      
## Running Tests

Ensure Kafka is up and running.

Start the spring boot service:

    ./run-service.sh

This script will present a menu of available tests to trigger the workflows:

    ./run-test.sh

Lastly, you can tail some of topics used:

- orders-inbox
- orders-outbox

In Kafka home dir:

```shell
bin/kafka-console-consumer.sh --topic orders-inbox --from-beginning --bootstrap-server localhost:9092 --property print.key=true
bin/kafka-console-consumer.sh --topic orders-outbox --from-beginning --bootstrap-server localhost:9092 --property print.key=true
```

