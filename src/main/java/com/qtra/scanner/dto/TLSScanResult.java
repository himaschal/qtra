package com.qtra.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TLSScanResult {
    private String domain;
    private String protocol;     // TLS version (e.g., TLS 1.3)
    private String cipherSuite;  // Negotiated cipher suite
}
