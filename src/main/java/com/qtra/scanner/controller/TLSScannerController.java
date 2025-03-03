package com.qtra.scanner.controller;

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

    @GetMapping("/scan")
    @Operation(summary = "Scan a domain and subdomains for TLS security")
    public ResponseEntity<String> scanDomain(@RequestParam String domain) {
        tlsScanner.scanAndPublish(domain);
        return ResponseEntity.ok("Scan initiated for " + domain);
    }

}

