# Verve Challenge Application

## Overview

The Verve Challenge Application is a Spring Boot-based REST API designed for processing unique ID requests and integrating with Redis and Kafka. This document provides instructions for building and running the application as a JAR file, as well as configuring the necessary properties.

## Prerequisites

- Java 21
- Maven 3
- Redis server (running on localhost:6379 or any server)
- Kafka broker (configured with the correct username and password)

## Configuration

The application uses the configuration properties, here, some of the important configurations can be seen:

```properties
server.port=8080
spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.kafka.properties.sasl.mechanism=PLAIN
spring.kafka.bootstrap-servers=<KAFKA_BROKER_IP>:9092
spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username='<KAFKA_USERNAME>' password='<KAFKA_PASSWORD>';
```
## Configuration Details

- **Server Port**: The application will run on port `8080`.

### Redis Configuration

- **Host**: `localhost`
- **Port**: `6379`

### Kafka Configuration
- Please create a topic called `topic_0` (The topic name can't be changed). 
- Replace `<KAFKA_BROKER_IP>`, `<KAFKA_USERNAME>`, and `<KAFKA_PASSWORD>` with the appropriate values for your Kafka setup.

## Running the Application

To run the application, use the following command:

```bash
java -jar challenge-0.0.1-SNAPSHOT.jar
```
Make sure to adjust(override) the configuration file, by passing the configuration as command-line arguments:
```bash
java -jar challenge-0.0.1-SNAPSHOT.jar --server.port=8080 --spring.data.redis.host=localhost --spring.data.redis.port=6379 --spring.kafka.bootstrap-servers=<KAFKA_BROKER_IP>:9092 --spring.kafka.properties.sasl.jaas.config="org.apache.kafka.common.security.plain.PlainLoginModule required username='<KAFKA_USERNAME>' password='<KAFKA_PASSWORD>';"
```

Example
```bash
java -jar challenge-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.data.redis.host=localhost --spring.data.redis.port=6379 --spring.kafka.bootstrap-servers="example-1.aws.confluent.cloud:9092" --spring.kafka.properties.sasl.jaas.config="org.apache.kafka.common.security.plain.PlainLoginModule required username='api_key_is_here' password='secret_key_is_here';"
```
## Accessing the API
Once the application is running, you can access the API at:

```bash
http://localhost:8080/api/verve/accept
```

## Example Request with Query Params
You can test the endpoint using a tool like Postman or cURL. Here’s an example cURL command:


```bash
curl -X GET "http://localhost:8080/api/verve/accept?id=1&endpoint=http://example.com"

```

## Load Testing with JMeter
You can use Apache JMeter to load test the application. Set up JMeter to simulate multiple concurrent users sending requests to the REST API.

### Load testing with random `id`
in **JMeter**'s Parameters with the Request tab add parameter `id` and value `${__Random(minNumber, maxNumber)}`