package com.verve.challenge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class for producing messages to a Kafka topic.
 * This service is responsible for sending unique ID counts to a specific Kafka topic.
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "topic_0";

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Constructor for KafkaProducerService.
     *
     * @param kafkaTemplate the KafkaTemplate used for producing messages to Kafka
     */
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a message containing the unique ID count to a Kafka topic.
     *
     * @param message the message to send to the Kafka topic, typically a string containing the count of unique IDs
     */
    public void sendUniqueIdCount(String message) {
        logger.info("Sending message to Kafka topic {}: {}", TOPIC, message);
        kafkaTemplate.send(TOPIC, message);
    }
}
