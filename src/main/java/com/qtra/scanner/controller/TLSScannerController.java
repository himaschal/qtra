package com.qtra.scanner.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.service.KafkaProducerService;
import com.qtra.scanner.service.TLSScanner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tls")
@Tag(name = "TLS Scanner API", description = "Endpoints for TLS scanning and security analysis")
@RequiredArgsConstructor
public class TLSScannerController {

    private final TLSScanner tlsScanner;
    private final KafkaProducerService kafkaProducerService;

    @GetMapping("/scan")
    @Operation(summary = "Scan a domain for TLS security", description = "Performs a TLS handshake and returns encryption details.")
    public ResponseEntity<TLSScanResult> scanDomain(@RequestParam String domain) {
        TLSScanResult result = tlsScanner.scan(domain);

        // Convert result to JSON (or any suitable format) for sending to Kafka
        String message = convertResultToJson(result);

        // Send scan result to Kafka
        kafkaProducerService.sendMessage("tls-scan-results", domain, message);

        return ResponseEntity.ok(result);
    }

    private String convertResultToJson(TLSScanResult result) {
        // Use your preferred method to convert the result to a JSON string
        // Example using ObjectMapper (you may need to import com.fasterxml.jackson.databind.ObjectMapper)
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}"; // return an empty JSON object in case of error
        }
    }
}
