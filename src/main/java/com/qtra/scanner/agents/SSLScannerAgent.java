package com.qtra.scanner.agents;

import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.service.TLSScanner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SSLScannerAgent {

    private final TLSScanner tlsScanner;

    public SSLScannerAgent(TLSScanner tlsScanner) {
        this.tlsScanner = tlsScanner;
    }

    public CompletableFuture<List<TLSScanResult>> process(String domain) {
        return tlsScanner.scanWithSubdomains(domain);
    }
}