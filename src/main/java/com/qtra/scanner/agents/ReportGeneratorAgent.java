package com.qtra.scanner.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.dto.QuantumGroupedReadinessResult;
import com.qtra.scanner.dto.QuantumReadinessResult;
import com.qtra.scanner.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportGeneratorAgent {

    private final ObjectMapper objectMapper;
    private final KafkaProducerService kafkaProducerService;

    @Value("${spring.kafka.topics.tls-report-results}")
    private String reportResultsTopic;

    public ReportGeneratorAgent(ObjectMapper objectMapper, KafkaProducerService kafkaProducerService) {
        this.objectMapper = objectMapper;
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = "${spring.kafka.topics.tls-quantum-results}", groupId = "report-generator-group")
    public void consumeAnalysisResults(String message) {
        try {
            // Deserialize as a grouped readiness result
            QuantumGroupedReadinessResult groupedResult = objectMapper.readValue(message, QuantumGroupedReadinessResult.class);
            log.info("Received grouped analysis result: {}", groupedResult);

            // Convert into a structured table format for Grafana
            List<Map<String, Object>> grafanaData = groupedResult.getSubdomainResults().stream().map(result -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("rootDomain", groupedResult.getRootDomain());
                row.put("subdomain", result.getDomain());
                row.put("safety_level", result.getSafetyLevel().toString());
                row.put("cipher_strength", result.getCipherStrengthScore());
                row.put("tls_version", result.getTlsVersionScore());
                row.put("pqc_certificate", result.getPqcCertificateScore());
                row.put("hsts", result.getHstsScore());
                row.put("dnssec", result.getDnssecScore());
                row.put("total_score", result.getTotalReadinessScore());
                return row;
            }).collect(Collectors.toList());

            // Convert to JSON and publish to Kafka
            String jsonReport = objectMapper.writeValueAsString(grafanaData);
            kafkaProducerService.sendMessage(reportResultsTopic, groupedResult.getRootDomain(), jsonReport);
            log.info("📤 Published structured report for {} to {}", groupedResult.getRootDomain(), reportResultsTopic);

        } catch (JsonProcessingException e) {
            log.error("Error processing Kafka message: ", e);
        }
    }
}
