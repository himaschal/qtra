package com.qtra.scanner.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.ai.VulnerabilityPredictor;
import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.service.KafkaProducerService;
import com.qtra.scanner.service.TLSScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SSLScannerAgent {

    private final TLSScanner tlsScanner;
    private final VulnerabilityPredictor predictor;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.tls-scan-results}")
    private String scanResultsTopic;

    @KafkaListener(topics = "${spring.kafka.topics.tls-scan-requests-topic}", groupId = "ssl-scanner-group")
    public void processScanRequest(String message) {
        try {
            log.info("📥 Received scan request for domain: {}", message);

            CompletableFuture<List<TLSScanResult>> futureScanResults = tlsScanner.scanWithSubdomains(message);

            futureScanResults.thenAccept(scanResults -> {
                List<TLSScanResult> enrichedResults = scanResults.stream()
                        .map(result -> new TLSScanResult(
                                result.getDomain(),
                                result.getProtocol(),
                                result.getCipherSuite(),
                                predictor.predictRisk(result) // Assign risk score
                        ))
                        .collect(Collectors.toList());

                try {
                    String jsonResults = objectMapper.writeValueAsString(enrichedResults);
                    kafkaProducerService.sendMessage(scanResultsTopic, message, jsonResults);
                    log.info("📤 Published TLS scan results for {}", message);
                } catch (Exception e) {
                    log.error("❌ Error serializing or publishing scan results: ", e);
                }
            });
        } catch (Exception e) {
            log.error("❌ Error processing scan request in SSLScannerAgent: ", e);
        }
    }
}
