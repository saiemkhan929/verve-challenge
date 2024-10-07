package com.verve.challenge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import com.verve.challenge.service.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller class for handling Verve API requests.
 * This controller uses asynchronous processing and scheduling tasks to manage
 * request acceptance, unique ID logging, and integration with Redis and Kafka.
 */
@RestController
@EnableScheduling
public class VerveController {

    private static final Logger logger = LoggerFactory.getLogger(VerveController.class);

    private final ThreadPoolTaskExecutor executor;
    private final WebClient webClient;
    private final RedisTemplate redisTemplate;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Constructor for VerveController.
     *
     * @param executor the thread pool task executor for asynchronous operations
     * @param webClientBuilder builder for creating WebClient instances
     * @param redisTemplate template for interacting with Redis
     * @param kafkaProducerService service for sending messages to Kafka
     */
    @Autowired
    public VerveController(ThreadPoolTaskExecutor executor, WebClient.Builder webClientBuilder, RedisTemplate redisTemplate, KafkaProducerService kafkaProducerService) {
        this.executor = executor;
        this.webClient = webClientBuilder.build();
        this.redisTemplate = redisTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Accepts incoming requests and processes them asynchronously.
     * The request is logged with a unique ID in Redis, and optionally sends a request to a specified endpoint.
     *
     * @param id the unique identifier for the request
     * @param endpoint an optional endpoint to send a POST request with the unique count
     * @return a CompletableFuture representing the asynchronous result of the request
     */
    @GetMapping("/api/verve/accept")
    public CompletableFuture<ResponseEntity<String>> acceptRequest(@RequestParam Integer id,
                                                                   @RequestParam(required = false) String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (id == null) {
                    return ResponseEntity.badRequest().body("Missing required 'id' parameter");
                }

                // Add the ID to a Redis Set, which ensures only unique IDs are stored
                String setKey = "unique:ids";
                redisTemplate.opsForSet().add(setKey, String.valueOf(id));  // Automatically handles duplicates

                // Set expiration for the set if needed (optional)
                redisTemplate.expire(setKey, 1, TimeUnit.MINUTES);

                // Asynchronously send request if the endpoint is provided
                if (endpoint != null) {
                    sendPostRequest(endpoint, getUniqueCount());
                }

                return ResponseEntity.ok("ok");
            } catch (Exception e) {
                logger.error("Error processing request", e);
                return ResponseEntity.status(500).body("failed");
            }
        }, executor); // Use the executor for asynchronous request handling
    }

    /**
     * Logs the number of unique requests every minute.
     * Uses a scheduled task to retrieve the unique count from Redis and sends it to Kafka.
     */
    @Scheduled(fixedRate = 60000)
    private void logUniqueRequests() {
        try {
            kafkaProducerService.sendUniqueIdCount(String.valueOf(getUniqueCount()));
        } catch (Exception e) {
            logger.error("Failed to send unique request count to Kafka", e);
        }
    }

    /**
     * Sends an HTTP POST request using WebClient with non-blocking I/O.
     *
     * @param endpoint the URL to which the POST request should be sent
     * @param count the unique count to include in the request payload
     */
    private void sendPostRequest(String endpoint, long count) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("unique_count", count);

        webClient.post()
                .uri(endpoint)
                .bodyValue(postData)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> logger.info("Response code from endpoint {}: {}", endpoint, response.getStatusCode()))
                .doOnError(error -> logger.error("Failed to send POST request to {}", endpoint, error))
                .subscribe(); // Non-blocking subscription
    }

    /**
     * Retrieves the unique request count from Redis and deletes the set after retrieval.
     *
     * @return the unique count of IDs stored in Redis
     */
    private long getUniqueCount() {
        String setKey = "unique:ids";
        long uniqueCount = redisTemplate.opsForSet().size(setKey);

        // Resetting the count for the next minute
        redisTemplate.delete(setKey);

        return uniqueCount;
    }
}
