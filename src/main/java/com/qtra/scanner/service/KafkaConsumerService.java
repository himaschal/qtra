package com.qtra.scanner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.agents.QuantumRiskAnalyzerAgent;
import com.qtra.scanner.agents.ReportGeneratorAgent;
import com.qtra.scanner.config.KafkaConsumerConfig;
import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.service.KafkaProducerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Collections;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaProducerService kafkaProducerService;
    private final QuantumRiskAnalyzerAgent quantumRiskAnalyzerAgent;
    private final ReportGeneratorAgent reportGeneratorAgent;
    private final ObjectMapper objectMapper;
    private String topic;

    public KafkaConsumerService(
            KafkaConsumer<String, String> kafkaConsumer,
            KafkaProducerService kafkaProducerService,
            QuantumRiskAnalyzerAgent quantumRiskAnalyzerAgent,
            ReportGeneratorAgent reportGeneratorAgent,
            ObjectMapper objectMapper,
            KafkaConsumerConfig kafkaConsumerConfig) {

        this.kafkaConsumer = kafkaConsumer;
        this.kafkaProducerService = kafkaProducerService;
        this.quantumRiskAnalyzerAgent = quantumRiskAnalyzerAgent;
        this.reportGeneratorAgent = reportGeneratorAgent;
        this.objectMapper = objectMapper;
        this.topic = kafkaConsumerConfig.getTopic();

        this.kafkaConsumer.subscribe(Collections.singletonList(topic));
        logger.info("Subscribed to Kafka topic: {}", topic);
    }

    /**
     * Polls Kafka messages every second and processes them.
     */
    @Scheduled(fixedDelay = 1000) // Runs every second
    public void pollMessages() {
        logger.info("Polling for messages...");

        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));

        if (records.isEmpty()) {
            logger.info("No messages found in tls-scan-results");
            return;
        }

        for (ConsumerRecord<String, String> record : records) {
            logger.info("Processing message from tls-scan-results: key={}, value={}", record.key(), record.value());
            processRecord(record);
        }

        kafkaConsumer.commitSync();
    }

    /**
     * Processes a single Kafka message.
     */
    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            String json = record.value();
            logger.info("Received message: key={}, value={}", record.key(), json);

            // Deserialize JSON into a list of TLSScanResults
            List<TLSScanResult> scanResults = objectMapper.readValue(json, new TypeReference<>() {});

            for (TLSScanResult scanResult : scanResults) {
                // Analyze quantum risk
                String quantumAnalysis = quantumRiskAnalyzerAgent.process(scanResult);
                logger.info("Quantum Analysis Result: {}", quantumAnalysis);

                // Generate report
                String report = reportGeneratorAgent.process(quantumAnalysis);
                logger.info("Generated Report: {}", report);

                // Send report to Kafka topic "tls-analysis-results"
                kafkaProducerService.sendMessage("tls-analysis-results", scanResult.getDomain(), report);
            }

        } catch (Exception e) {
            logger.error("Error processing Kafka message: key={}, value={}", record.key(), record.value(), e);
        }
    }
}

