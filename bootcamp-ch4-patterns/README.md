# Chapter 4 :: Patterns

Module for demonstrating various design patterns, mainly the transactional outbox.

Key topics:

- Outbox pattern via CDC
- Inbox pattern via CDC

## Prerequisites

Kafka is required to demonstrate CDC. You can either use a manged Kafka cluster or a
local self-hosted setup. In the latter case, just follow the [quickstart](https://kafka.apache.org/quickstart) guidelines
to setup a vanilla Kafka instance.

Depending on your network setup, you may need to edit the following in `config/server.properties`:

    listeners=PLAINTEXT://..
    advertised.listener=PLAINTEXT://

Then start Kafka in daemon mode:

    bin/kafka-server-start.sh -daemon config/server.properties

To tail some topic, in this case `hello`:

    bin/kafka-console-consumer.sh --topic hello --from-beginning --bootstrap-server localhost:9092 --property print.key=true

## Running Tests

This script will present a menu of available test:

    ./run-test.sh

