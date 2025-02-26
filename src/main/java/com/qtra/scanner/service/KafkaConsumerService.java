package com.qtra.scanner.service;

//import com.qtra.scanner.config.KafkaConsumerConfig;
//import jakarta.annotation.PostConstruct;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.util.Collections;
//
//@Service
//public class KafkaConsumerService {
//
//    private final KafkaConsumer<String, String> kafkaConsumer;
//    private final String topic;
//
//    @Autowired
//    public KafkaConsumerService(KafkaConsumer<String, String> kafkaConsumer, KafkaConsumerConfig kafkaConsumerConfig) {
//        this.kafkaConsumer = kafkaConsumer;
//        this.topic = kafkaConsumerConfig.getTopic();
//        this.kafkaConsumer.subscribe(Collections.singletonList(topic));
//    }
//
//    @PostConstruct
//    public void startConsumer() {
//        new Thread(this::pollMessages, "Kafka-Consumer-Thread").start();
//    }
//
//    private void pollMessages() {
//        while (true) {
//            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
//            for (ConsumerRecord<String, String> record : records) {
//                System.out.printf("Consumed message: key=%s value=%s%n", record.key(), record.value());
//            }
//        }
//    }
//}


import com.qtra.scanner.agents.QuantumRiskAnalyzerAgent;
import com.qtra.scanner.agents.ReportGeneratorAgent;
import com.qtra.scanner.agents.SSLScannerAgent;
import com.qtra.scanner.config.KafkaConsumerConfig;
import com.qtra.scanner.dto.TLSScanResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@EnableScheduling
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaProducerService kafkaProducerService;
    private final SSLScannerAgent sslScannerAgent;
    private final QuantumRiskAnalyzerAgent quantumRiskAnalyzerAgent;
    private final ReportGeneratorAgent reportGeneratorAgent;
    private final String topic;

    public KafkaConsumerService(
            KafkaConsumer<String, String> kafkaConsumer,
            KafkaProducerService kafkaProducerService,
            SSLScannerAgent sslScannerAgent,
            QuantumRiskAnalyzerAgent quantumRiskAnalyzerAgent,
            ReportGeneratorAgent reportGeneratorAgent,
            KafkaConsumerConfig kafkaConsumerConfig) {

        this.kafkaConsumer = kafkaConsumer;
        this.kafkaProducerService = kafkaProducerService;
        this.sslScannerAgent = sslScannerAgent;
        this.quantumRiskAnalyzerAgent = quantumRiskAnalyzerAgent;
        this.reportGeneratorAgent = reportGeneratorAgent;
        this.topic = kafkaConsumerConfig.getTopic();

        this.kafkaConsumer.subscribe(Collections.singletonList(topic));
        logger.info("Subscribed to Kafka topic: {}", topic);
    }

    /**
     * Polls Kafka messages every second and processes them.
     */
    @Scheduled(fixedDelay = 1000) // Runs every 1 second
    public void pollMessages() {
        try {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                return; // No messages to process
            }

            for (ConsumerRecord<String, String> record : records) {
                processRecord(record);
            }

            kafkaConsumer.commitSync(); // Commit offsets after processing all messages
        } catch (Exception e) {
            logger.error("Error while polling messages from Kafka", e);
        }
    }

    /**
     * Processes a single Kafka message.
     */
    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            String input = record.value();
            logger.info("Received message: key={}, value={}", record.key(), input);

            // Call SSLScannerAgent to get the TLSScanResult
            TLSScanResult scanResult = sslScannerAgent.process(input);

            // Pass the TLSScanResult to QuantumRiskAnalyzerAgent for analysis
            String quantumResult = quantumRiskAnalyzerAgent.process(scanResult);

            // Generate report with the results
            String report = reportGeneratorAgent.process(quantumResult);

            // Send final report to Kafka
            kafkaProducerService.sendMessage("tls-analysis-results", record.key(), report);
        } catch (Exception e) {
            logger.error("Error processing Kafka message: key={}, value={}", record.key(), record.value(), e);
        }
    }
}
