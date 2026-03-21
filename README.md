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
a typical spring boot application stack. Many of these topics are also covered in 
this [blog](https://blog.cloudneutral.se/).

# Modules

| Capter           | Chapter                                                    | Description                                              |
|------------------|------------------------------------------------------------|----------------------------------------------------------|
| (1) Transactions | [Transactions](spring-bootcamp-ch1-transactions/README.md) | Transaction management including retries, timeouts, etc. |
| (2) Contention   | [Contention](spring-bootcamp-ch2-contention/README.md)     | Transaction contention mitigation                        |
| -                | [CTE](spring-bootcamp-ch2-cte/README.md)                   | Modifying common table expressions                       |
| (3) Performance  | [Caching](spring-bootcamp-ch3-caching/README.md)           | Cache invalidation through CDC                           |
| -                | [Batching](spring-bootcamp-ch3-batching/README.md)         | Batch inserts, updates and upserts                       |
| -                | [Followers](spring-bootcamp-ch3-followers/README.md)       | Bounded and exact staleness reads                        |
| (4) Patterns     | [Idempotency](spring-bootcamp-ch4-idempotency/README.md)   | Service level idempotency by de-duplication              |
| -                | [Inbox](spring-bootcamp-ch4-inbox/README.md)               | Transactional inbox pattern through CDC                  |
| -                | [Outbox](spring-bootcamp-ch4-outbox/README.md)             | Transactional outbox pattern through CDC                 |
| -                | [Parallel Queries](spring-bootcamp-ch4-parallel/README.md) | Parallel fork/join query execution pattern               |
| -                | [Locking](spring-bootcamp-ch4-locking/README.md)           | Locking patterns equivalent to PG advisory locks         |

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
- Kafka 3.6+
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

    git clone git@github.com:kai-niemi/spring-bootcamp.git && cd bootcamp

### Build the artifact

    chmod +x mvnw
    ./mvnw clean install

# Running

Pick the training chapter you want to run a test in (selected from menu), for example chapter 1:

    spring-bootcamp-ch1-transactions/run-test.sh

# Terms of Use

Use of this project is entirely at your own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) for terms and conditions.