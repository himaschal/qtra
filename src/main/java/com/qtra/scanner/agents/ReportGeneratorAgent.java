package com.qtra.scanner.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.dto.QuantumReadinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportGeneratorAgent {

    private final ObjectMapper objectMapper;

    public ReportGeneratorAgent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${spring.kafka.topics.tls-quantum-results}", groupId = "report-generator-group")
    public void consumeQuantumResults(String message) {
        try {
            QuantumReadinessResult readinessResult = objectMapper.readValue(message, QuantumReadinessResult.class);
            generateReport(readinessResult);
        } catch (Exception e) {
            log.error("❌ Error processing Kafka message: ", e);
        }
    }

    private void generateReport(QuantumReadinessResult readinessResult) {
        String report = String.format("""
            📌 **Quantum Readiness Report for %s**
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

        log.info(report);
    }
}
