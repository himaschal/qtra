package com.qtra.scanner.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qtra.scanner.dto.QuantumReadinessResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class ReportGeneratorAgent {

    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorAgent.class);

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final ObjectMapper objectMapper;

    public ReportGeneratorAgent(KafkaConsumer<String, String> kafkaConsumer, ObjectMapper objectMapper) {
        this.kafkaConsumer = kafkaConsumer;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.kafkaConsumer.subscribe(Collections.singletonList("tls-analysis-results"));
        logger.info("Subscribed to Kafka topic: tls-analysis-results");
    }

    @Scheduled(fixedDelay = 1000)
    public void pollMessages() {
        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
            processRecord(record);
        }
        kafkaConsumer.commitSync();
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            String json = record.value();

            // Deserialize as a list instead of a single object
            List<QuantumReadinessResult> readinessResults = objectMapper.readValue(json, new TypeReference<>() {
            });

            for (QuantumReadinessResult result : readinessResults) {
                generateReport(result);
            }

        } catch (Exception e) {
            logger.error("Error processing Kafka message: ", e);
        }
    }

    public void generateReport(QuantumReadinessResult readinessResult) {
        String report = String.format("""
            📌 Quantum Readiness Report for %s
            --------------------------------------------------
            🔹 Quantum Safety Level: %s
            🔹 Cipher Strength Score: %.1f/40
            🔹 TLS Version Score: %.1f/20
            🔹 PQC Certificate Score: %.1f/20
            🔹 HSTS Score: %.1f/10
            🔹 DNSSEC Score: %.1f/10
            --------------------------------------------------
            ✅ **Total Quantum Readiness Score: %.1f/100**
            """,
                readinessResult.getDomain(),
                readinessResult.getSafetyLevel(),
                readinessResult.getCipherStrengthScore(),
                readinessResult.getTlsVersionScore(),
                readinessResult.getPqcCertificateScore(),
                readinessResult.getHstsScore(),
                readinessResult.getDnssecScore(),
                readinessResult.getTotalReadinessScore());

        logger.info(report);
    }
}
