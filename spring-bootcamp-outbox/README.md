# Chapter 5 :: Outbox Pattern

Module for demonstrating the transactional outbox pattern using CDC and Kafka.

Key topics:

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

```shell
bin/kafka-console-consumer.sh --topic orders-outbox --from-beginning --bootstrap-server localhost:9092 --property print.key=true
```

