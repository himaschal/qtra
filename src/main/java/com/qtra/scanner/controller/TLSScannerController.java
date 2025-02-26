package com.qtra.scanner.controller;

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

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/tls")
@Tag(name = "TLS Scanner API", description = "Endpoints for TLS scanning and security analysis")
@RequiredArgsConstructor
public class TLSScannerController {

    private final TLSScanner tlsScanner;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @GetMapping("/scan")
    @Operation(summary = "Scan a domain and subdomains for TLS security")
    public CompletableFuture<ResponseEntity<List<TLSScanResult>>> scanDomain(@RequestParam String domain) {
        return tlsScanner.scanWithSubdomains(domain).thenApply(results -> {
            try {
                String jsonResult = objectMapper.writeValueAsString(results);
                kafkaProducerService.sendMessage("tls-scan-results", domain, jsonResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseEntity.ok(results);
        });
    }
}

