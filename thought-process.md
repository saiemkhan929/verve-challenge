# Implementation Approach

## Step 1: Building a Java REST Service with Spring Boot

### Requirement:
- Build a REST service capable of processing **10K requests per second**.
- The service exposes a `GET` endpoint `/api/verve/accept` that accepts:
    - An integer `id` as a mandatory query parameter.
    - An optional `endpoint` parameter that allows making HTTP requests.
    - The service should return:
        - `"ok"` if processed successfully.
        - `"failed"` if there were errors during processing.

### Implementation Approach:
1. **Spring Boot Setup**:
    - Used **Spring Boot** to quickly set up the REST API.
    - Spring Boot allows high throughput and provides essential tools like dependency management, an embedded server, and easy integration with other libraries.

2. **Handling Requests with `GET`**:
    - Created the `/api/verve/accept` endpoint using **Spring’s `@GetMapping`**.
    - The method accepts an `id` (integer) and an optional `endpoint` (string).
    - If an `endpoint` is provided, the service makes an HTTP request using **Spring’s WebClient**.

3. **Concurrency and Thread-Safety**:
    - To handle **10K requests per second**, I used a **ConcurrentHashMap** to store unique IDs in a thread-safe manner.
    - Requests were processed asynchronously using **ThreadPoolTaskExecutor**.
    - This ensured that the application could process high volumes of requests without blocking threads.

4. **Return Value**:
    - If there were no errors, the service returned `"ok"`.
    - In case of any errors, it returned `"failed"`, logging the error for further analysis.

### Design Considerations:
- **Concurrency**: The `ConcurrentHashMap` ensures that requests are processed without race conditions.
- **Asynchronous Execution**: The use of `CompletableFuture` and `ThreadPoolTaskExecutor` guarantees that requests are processed in parallel, improving the overall throughput.
- **Scalability**: The design focuses on scalability, allowing the application to handle high loads efficiently.

---

## Step 2: Logging Unique Requests Every Minute

### Requirement:
- The service needs to log the count of **unique requests** received in the last minute.
- Uniqueness is determined based on the `id` provided.

### Implementation Approach:
1. **Unique ID Tracking**:
    - The `id` was stored in a **ConcurrentHashMap** to track unique requests.
    - Each `id` was stored as a key with a Boolean value, ensuring that only unique requests were counted.

2. **Scheduled Task for Logging**:
    - I used **Spring’s `@Scheduled` annotation** to run a task every minute to count the number of unique requests.
    - Every minute, the size of the `ConcurrentHashMap` was logged, and the map was cleared for the next cycle.

3. **Reset Mechanism**:
    - After logging the unique request count, the `ConcurrentHashMap` was reset to track the next set of unique requests.

### Design Considerations:
- **Concurrency**: The `ConcurrentHashMap` was used for thread-safe access, allowing multiple threads to add IDs simultaneously.
- **Logging**: I chose a standard logger (Logback) for asynchronous logging, ensuring that logging would not slow down the application.

---

## Extension 1: POST Request Instead of GET

### Requirement:
- Instead of firing an HTTP **GET** request to the provided `endpoint`, change it to a **POST** request.
- The data structure for the request body can be freely chosen.

### Implementation Approach:
1. **POST Request with WebClient**:
    - I modified the method that sends HTTP requests to use **WebClient’s `POST` method** instead of `GET`.
    - The request body was structured as a simple JSON object containing the `unique_count` (number of unique requests).

2. **JSON Payload**:
    - The payload sent in the POST request contains the following structure:
   ```json
   {
     "unique_count": <count>
   }

This data is sent to the `endpoint` provided in the query parameter.

### Design Considerations:

- **Non-blocking I/O**: The use of `WebClient` ensures that HTTP requests are non-blocking, allowing the application to continue processing other tasks while waiting for responses.
- **POST Request Structure**: The structure of the POST request was kept simple and minimal to ensure high throughput and fast processing.

---

## Extension 2: Distributed Deduplication Behind a Load Balancer

### Requirement:

- Ensure that the deduplication of `id`s works even when the service is deployed behind a **load balancer** with multiple instances.
- No separate deployment for the load balancer; the deduplication should be handled within the application.

### Implementation Approach:

1. **Use of Redis**:
    - To handle deduplication across multiple instances, I introduced **Redis** as a distributed in-memory data store. Additionally, instead of cloud server, I've deployed redis locally. However, Please read *Readme.md* file for configuration details.
    - Each instance of the application would check Redis for the existence of an `id` before processing it. If the `id` already exists in Redis, it is not processed again.

2. **`setIfAbsent` for Deduplication**:
    - Redis’s **SETNX** (Set if Not Exists) functionality, implemented using `setIfAbsent()` in **Spring Redis**, ensures that only the first instance to process a given `id` successfully stores it.
    - Redis was configured with an expiration policy so that IDs would automatically expire after 1 minute, preventing the store from growing indefinitely.

3. **Thread-Safe and Distributed**:
    - Redis acts as a centralized store for all instances, ensuring that no duplicate processing occurs even if two instances receive the same `id` simultaneously.

---

## Extension 3: Send Unique ID Count to Kafka

### Requirement:

- Instead of logging the count of unique IDs to a file, send the count to a distributed streaming service like **Kafka**.

### Implementation Approach:

1. **Kafka as a Streaming Service**:
    - I integrated **Apache Kafka** as the distributed streaming service.
    - A new **KafkaProducerService** was created to send the count of unique requests to a Kafka topic named `topic_0`. I've used `Confluent Cloud` Data Streaming platform to test Kafka. Please read *Readme.md* file for setup details.  

2. **Scheduled Task for Sending Counts**:
    - The scheduled task was modified to send the count of unique requests every minute to Kafka, instead of logging it.
    - The unique request count is retrieved from Redis (since Redis now manages the deduplication of IDs).

3. **Kafka Producer Configuration**:
    - Configured **Spring Kafka** with the necessary producer settings, including Kafka brokers, serializers, and retries to ensure reliable message delivery.

### Design Considerations:

- **Real-Time Streaming**: Kafka was chosen for its high-throughput, distributed messaging capabilities, ensuring that the unique ID counts are streamed in real-time.
- **Asynchronous Message Delivery**: Kafka’s message delivery mechanism is asynchronous, allowing the system to continue processing while messages are being sent.

---

### Tools and Technologies Used:

- **Spring Boot**: For building the REST API quickly and efficiently.
- **ConcurrentHashMap**: To store unique `id`s locally in a thread-safe manner.
- **Redis**: For distributed deduplication across multiple instances.
- **Apache Kafka**: As the distributed streaming service for sending unique ID counts.
- **Spring WebClient**: For non-blocking HTTP POST requests.
- **Spring Scheduling**: To handle recurring tasks (e.g., logging, sending counts to Kafka).
- **Apache JMeter**: For load testing and performance evaluation of the REST API.

### Limitations

The testing of the Verve Challenge Application faced several limitations due to constrained hardware resources, which impacted the ability to conduct extensive load testing effectively. I utilized network protocols for communication between devices and performed tests on an AWS EC2 instance equipped with an optimized CPU. While this setup worked as expected, the scope of testing was primarily limited to basic functionalities, preventing comprehensive evaluations involving high concurrency or varying message sizes. Additionally, the lack of distributed load testing further restricted my ability to assess the application's performance under heavy load conditions. Although the integration with Kafka for writing data was smooth, addressing these limitations in future iterations will be essential for gaining a deeper understanding of the application's scalability and resilience.
