<p>	
	<a href="https://github.com/kai-niemi/spring-bootcamp/actions/workflows/maven.yml"><img src="https://github.com/kai-niemi/spring-bootcamp/actions/workflows/maven.yml/badge.svg?branch=main" alt="">
</p>

<!-- TOC -->
* [About](#about)
* [Modules](#modules)
* [Compatibility](#compatibility)
* [Setup](#setup)
  * [Prerequisites](#prerequisites)
  * [Install the JDK](#install-the-jdk)
  * [Database Setup](#database-setup)
  * [Building](#building)
    * [Clone the project](#clone-the-project)
    * [Build the artifact](#build-the-artifact)
* [Running](#running)
* [Terms of Use](#terms-of-use)
<!-- TOC -->

# About

<img align="left" src="logo.png" width="64"/> 

Spring Boot v4 bootcamp modules covering various topics relevant to development against CockroachDB using
a typical spring boot application stack.

# Modules

- Chapter 1
  - [Transactions](bootcamp-ch1-transactions/README.md)
- Chapter 2
  - [Contention](bootcamp-ch2-contention/README.md)
- Chapter 3
  - [Performance](bootcamp-ch3-performance/README.md)
- Chapter 4 - Patterns
  - [Idempotency](bootcamp-ch4-idempotency/README.md)
  - [Inbox](bootcamp-ch4-inbox/README.md)
  - [Outbox](bootcamp-ch4-outbox/README.md)
  - [Parallel Queries](bootcamp-ch4-parallel/README.md)
  - [Locking](bootcamp-ch4-locking/README.md)

# Compatibility

- MacOS
- Linux
- JDK 21+ (LTS)
- CockroachDB v23+

# Setup

Things you need to build and run the modules locally.

## Prerequisites

- Java 21+ JDK
    - https://openjdk.org/projects/jdk/21/
    - https://www.oracle.com/java/technologies/downloads/#java21
- Git
    - https://git-scm.com/downloads/mac
- Kafka 3.6+ (for [bootcamp-ch5-cdc](bootcamp-ch5-cdc) only)
    - https://kafka.apache.org/downloads

## Install the JDK

MacOS (using sdkman):

    curl -s "https://get.sdkman.io" | bash
    sdk list java
    sdk install java 21.0 (use TAB to pick edition)  

Ubuntu:

    sudo apt-get install openjdk-21-jdk

## Database Setup

See [start a local cluster](https://www.cockroachlabs.com/docs/v24.2/start-a-local-cluster)
for setup instructions. You can also use CockroachDB Cloud (basic, standard or advanced).

Then create the database, for an insecure cluster:

    cockroach sql --insecure -e "create database bootcamp"

alternatively, for a secure cluster:

    cockroach sql --certs-dir=certs -e "CREATE DATABASE bootcamp; ALTER ROLE root WITH PASSWORD 'cockroach'"

An [enterprise license](https://www.cockroachlabs.com/docs/stable/licensing-faqs.html#obtain-a-license) is needed for some of the chapters that 
use enterprise features like follower reads and CDC.

## Building

### Clone the project

    git clone git@github.com:kai-niemi/spring-bootcamp.git && cd bootcamp-modules

### Build the artifact

    chmod +x mvnw
    ./mvnw clean install

# Running

Pick the training chapter you want to run the tests in, for example chapter 1:

    cd bootcamp-ch1-transactions

Then run the test starter script which will present a menu of options:

    ./run-test.sh

# Terms of Use

Use of this project is entirely at your own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) for terms and conditions.