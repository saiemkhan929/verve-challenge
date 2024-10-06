package com.verve.challenge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class VerveController {

    private final ConcurrentMap<Integer, Boolean> uniqueIds = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(VerveController.class);

    @GetMapping("/api/verve/accept")
    public ResponseEntity<String> accept(@RequestParam("id") int id,
                                         @RequestParam(value = "endpoint", required = false) String endpoint) {
        try {
            uniqueIds.put(id, true); // Adding to the map ensures uniqueness

            if (endpoint != null) {
                sendHttpRequest(endpoint);
            }

            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error processing request", e);
            return new ResponseEntity<>("failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendHttpRequest(String endpoint) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = endpoint + "?uniqueCount=" + uniqueIds.size();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("Sent request to: " + url + ", Status code: " + response.getStatusCode());
        } catch (Exception e) {
            logger.error("Error making HTTP request to " + endpoint, e);
        }
    }
    @Scheduled(fixedRate = 60000)
    public void logUniqueRequestCount() {
        int uniqueCount = uniqueIds.size();
        uniqueIds.clear(); // Clear the map for the next minute
        logger.info("Unique requests in the last minute: " + uniqueCount);
    }
}
