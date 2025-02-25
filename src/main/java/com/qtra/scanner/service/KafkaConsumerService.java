package com.qtra.scanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.dto.TLSScanResult;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

//@Service
public class KafkaConsumerService {

    private final KafkaConsumer<String, String> consumer;
    private final SSLScannerAgent sslScannerAgent; // Instance of SSLScannerAgent for processing scan results
    private volatile boolean running = true; // Flag to control the consumer's operation
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor for KafkaConsumerService.
     *
     * @param bootstrapServers Kafka bootstrap servers configuration.
     * @param groupId         Consumer group ID for tracking offsets.
     * @param topic           Topic to subscribe to for messages.
     * @param sslScannerAgent  Instance of SSLScannerAgent for processing scan results.
     */
    public KafkaConsumerService(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.topic}") String topic,
            SSLScannerAgent sslScannerAgent) { // Inject SSLScannerAgent

        // Set Kafka consumer properties
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start reading from the earliest message

        // Create the Kafka consumer instance with the specified properties
        this.consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic)); // Subscribe to the specified topic
        this.sslScannerAgent = sslScannerAgent; // Initialize SSLScannerAgent
        System.out.println("KafkaConsumerService initialized. Subscribed to topic: " + topic); // Log initialization
    }

    /**
     * Initialization method called after the service is constructed.
     * Starts consuming messages from Kafka.
     */
    @PostConstruct
    public void init() {
        System.out.println("Starting to consume messages from Kafka..."); // Log the start of consumption
        consumeMessages(); // Start consuming messages after initialization
    }

    /**
     * Asynchronously consumes messages from the Kafka topic.
     * Processes each received message using SSLScannerAgent.
     */
    @Async
    public void consumeMessages() {
        System.out.println("Kafka message consumption thread started."); // Log thread start
        while (running) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                System.out.println("Received: " + record.value());
                TLSScanResult result = convertJsonToResult(record.value());
                String analysisResult = sslScannerAgent.analyze(result); // Use the analyze method to get insights
                System.out.println("Analysis Result: \n" + analysisResult); // Print the analysis result
            });
        }
    }

    public static TLSScanResult convertJsonToResult(String json) {
        try {
            return objectMapper.readValue(json, TLSScanResult.class);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + json);
            e.printStackTrace();
            return new TLSScanResult("UNKNOWN", "UNKNOWN", "UNKNOWN", null);
        }
    }
    /**
     * Cleanly shuts down the Kafka consumer when the service is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        running = false; // Stop the consumer loop
        consumer.wakeup();  // Interrupts poll() to exit cleanly
        consumer.close(); // Close the consumer to release resources
        System.out.println("Kafka Consumer shut down."); // Log the shutdown
    }
}
