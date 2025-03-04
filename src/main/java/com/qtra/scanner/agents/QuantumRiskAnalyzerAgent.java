package com.qtra.scanner.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.dto.QuantumGroupedReadinessResult;
import com.qtra.scanner.dto.QuantumReadinessResult;
import com.qtra.scanner.dto.TLSGroupedScanResult;
import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.enums.QuantumSafetyLevel;
import com.qtra.scanner.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class QuantumRiskAnalyzerAgent {

    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.tls-quantum-results}")
    private String quantumResultsTopic;

    @Autowired
    public QuantumRiskAnalyzerAgent(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = new ObjectMapper();
    }

    private static final List<String> TRULY_QUANTUM_SAFE_CIPHERS = List.of(
            "TLS_KYBER768_WITH_AES_128_GCM_SHA256",
            "TLS_KYBER1024_WITH_AES_256_GCM_SHA384"
    );

    private static final List<String> PQR_BUT_NOT_QUANTUM_SAFE_CIPHERS = List.of(
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256"
    );

    @KafkaListener(topics = "${spring.kafka.topics.tls-scan-results}", groupId = "ai-agent-group")
    public void analyzeTLSScanResults(String message) {
        try {
            // Deserialize grouped results
            TLSGroupedScanResult groupedResult = objectMapper.readValue(message, TLSGroupedScanResult.class);

            List<QuantumReadinessResult> analyzedResults = groupedResult.getSubdomains().stream()
                    .map(this::analyze)  // Apply quantum risk analysis
                    .toList();

            // Aggregate overall quantum readiness score for the root domain
            double avgReadinessScore = analyzedResults.stream()
                    .mapToDouble(QuantumReadinessResult::getTotalReadinessScore)
                    .average().orElse(0.0);

            // Create grouped quantum readiness result
            QuantumGroupedReadinessResult finalResult = new QuantumGroupedReadinessResult(
                    groupedResult.getRootDomain(), analyzedResults, avgReadinessScore
            );

            // Publish the aggregated result
            String jsonResult = objectMapper.writeValueAsString(finalResult);
            kafkaProducerService.sendMessage(quantumResultsTopic, groupedResult.getRootDomain(), jsonResult);

        } catch (Exception e) {
            log.error("Error processing TLS scan results", e);
        }

    }

    public QuantumReadinessResult analyze(TLSScanResult scanResult) {
        QuantumSafetyLevel safetyLevel = classifyCipher(scanResult.getCipherSuite());
        boolean hstsEnabled = checkHSTS(scanResult.getDomain());
        boolean dnssecEnabled = checkDNSSEC(scanResult.getDomain());

        double cipherStrengthScore = calculateCipherStrengthScore(scanResult.getCipherSuite());
        double tlsVersionScore = calculateTLSVersionScore(scanResult.getProtocol());
        double pqcCertificateScore = calculatePQCCertificateScore(scanResult.getDomain());
        double hstsScore = hstsEnabled ? 10.0 : 0.0;
        double dnssecScore = dnssecEnabled ? 10.0 : 0.0;
        double totalScore = cipherStrengthScore + tlsVersionScore + pqcCertificateScore + hstsScore + dnssecScore;

        log.info("🔍 Analyzed {}: SafetyLevel={}, Cipher={}, TLS={}, PQC={}, HSTS={}, DNSSEC={}, Total={}",
                scanResult.getDomain(), safetyLevel, cipherStrengthScore, tlsVersionScore, pqcCertificateScore, hstsScore, dnssecScore, totalScore);

        return new QuantumReadinessResult(scanResult.getDomain(), safetyLevel, cipherStrengthScore,
                tlsVersionScore, pqcCertificateScore, hstsScore, dnssecScore, totalScore);
    }

    private QuantumSafetyLevel classifyCipher(String cipherSuite) {
        if (TRULY_QUANTUM_SAFE_CIPHERS.contains(cipherSuite)) {
            return QuantumSafetyLevel.TRULY_QUANTUM_SAFE;
        } else if (PQR_BUT_NOT_QUANTUM_SAFE_CIPHERS.contains(cipherSuite)) {
            return QuantumSafetyLevel.PQR_BUT_NOT_QUANTUM_SAFE;
        } else {
            return QuantumSafetyLevel.NOT_QUANTUM_SAFE;
        }
    }

    private double calculateCipherStrengthScore(String cipherSuite) {
        if (TRULY_QUANTUM_SAFE_CIPHERS.contains(cipherSuite)) {
            return 40.0;
        } else if (PQR_BUT_NOT_QUANTUM_SAFE_CIPHERS.contains(cipherSuite)) {
            return 20.0;
        }
        return 0.0;
    }

    private double calculateTLSVersionScore(String protocol) {
        return switch (protocol) {
            case "TLSv1.3" -> 20.0;
            case "TLSv1.2" -> 10.0;
            default -> 0.0;
        };
    }

    private double calculatePQCCertificateScore(String domain) {
        // Placeholder: This should query crt.sh or Censys API
        String certAlgorithm = "RSA";  // Simulated response
        if (certAlgorithm.contains("SPHINCS+") || certAlgorithm.contains("Dilithium")) {
            return 20.0;
        } else if (certAlgorithm.contains("RSA") || certAlgorithm.contains("ECC")) {
            return 10.0;
        }
        return 0.0;
    }

    private boolean checkHSTS(String domain) {
        try {
            URL url = new URL("https://" + domain);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            return connection.getHeaderField("Strict-Transport-Security") != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDNSSEC(String domain) {
        try {
            Lookup lookup = new Lookup(domain, Type.DNSKEY);
            lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                for (Record record : lookup.getAnswers()) {
                    if (record instanceof DNSKEYRecord) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
