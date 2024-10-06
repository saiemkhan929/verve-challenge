package com.verve.challenge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
public class VerveController {

    private static final Logger logger = LoggerFactory.getLogger(VerveController.class);

    private final ThreadPoolTaskExecutor executor;
    private final WebClient webClient;
    private final StringRedisTemplate redisTemplate;  // Redis template for storing unique IDs

    @Autowired
    public VerveController(ThreadPoolTaskExecutor executor, WebClient.Builder webClientBuilder, StringRedisTemplate redisTemplate) {
        this.executor = executor;
        this.webClient = webClientBuilder.build();
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/api/verve/accept")
    public CompletableFuture<ResponseEntity<String>> acceptRequest(@RequestParam int id,
                                                                   @RequestParam(required = false) String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Use Redis to store unique IDs across instances
                String key = "unique:id:" + id;
                ValueOperations<String, String> operations = redisTemplate.opsForValue();

                // Check if the ID already exists, if not, add it
                Boolean isUnique = operations.setIfAbsent(key, "1");

                if (isUnique != null && isUnique) {
                    // Set expiration for the key so it auto-expires after a certain time
                    redisTemplate.expire(key, 1, TimeUnit.MINUTES);

                    // Asynchronously send request if the endpoint is provided
                    if (endpoint != null) {
                        sendPostRequest(endpoint, getUniqueCount());
                    }
                }

                return ResponseEntity.ok("ok");
            } catch (Exception e) {
                logger.error("Error processing request", e);
                return ResponseEntity.status(500).body("failed");
            }
        }, executor); // Use the executor for asynchronous request handling
    }

    // Log unique requests every minute using a scheduled task
    @Scheduled(fixedRate = 60000)
    private void logUniqueRequests() {
        long uniqueCount = redisTemplate.keys("unique:id:*").size();
        logger.info("Unique requests in the last minute: {}", uniqueCount);
    }

    // Send an HTTP POST request using WebClient for non-blocking I/O
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

    @PostMapping("/api/verve/verify")
    public ResponseEntity<String> verifyEndpoint(@RequestParam int uniqueCount, @RequestBody Map<String, Object> requestData) {
        logger.info("Received verification request with data: {}", requestData);
        return ResponseEntity.ok("Verification successful");
    }

    // Helper method to get the count of unique IDs in Redis
    private long getUniqueCount() {
        return redisTemplate.keys("unique:id:*").size();
    }
}
