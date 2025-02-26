package com.qtra.scanner.service;

import com.qtra.scanner.enums.QuantumSafetyLevel;
import com.qtra.scanner.dto.TLSScanResult;
import org.springframework.stereotype.Service;
import javax.net.ssl.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TLSScanner {

    private static final List<String> TRULY_QUANTUM_SAFE_CIPHERS = List.of(
            "TLS_KYBER768_WITH_AES_128_GCM_SHA256",
            "TLS_KYBER1024_WITH_AES_256_GCM_SHA384"
    );

    private static final List<String> PQR_BUT_NOT_QUANTUM_SAFE_CIPHERS = List.of(
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256"
    );

    private static final List<String> COMMON_SUBDOMAINS = List.of("www", "api", "mail", "blog", "shop");

    public CompletableFuture<List<TLSScanResult>> scanWithSubdomains(String domain) {
        List<CompletableFuture<TLSScanResult>> futures = new ArrayList<>();

        // Scan the main domain
        futures.add(CompletableFuture.supplyAsync(() -> scan(domain)));

        // Scan common subdomains asynchronously
        for (String subdomain : COMMON_SUBDOMAINS) {
            String subdomainToScan = subdomain + "." + domain;
            futures.add(CompletableFuture.supplyAsync(() -> scan(subdomainToScan)));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<TLSScanResult> results = new ArrayList<>();
                    for (CompletableFuture<TLSScanResult> future : futures) {
                        results.add(future.join());
                    }
                    return results;
                });
    }

    public TLSScanResult scan(String domain) {
        try {
            SSLSocket sslSocket = createSSLSocket(domain, 443);
            SSLSession session = sslSocket.getSession();

            String protocol = session.getProtocol();
            String cipherSuite = session.getCipherSuite();
            QuantumSafetyLevel safetyLevel = classifyCipher(cipherSuite);

            sslSocket.close();
            return new TLSScanResult(domain, protocol, cipherSuite, safetyLevel);
        } catch (Exception e) {
            return new TLSScanResult(domain, "UNKNOWN", "UNKNOWN", QuantumSafetyLevel.NOT_QUANTUM_SAFE);
        }
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

    private SSLSocket createSSLSocket(String domain, int port) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(domain, port);
        socket.setEnabledProtocols(socket.getSupportedProtocols());
        socket.startHandshake();
        return socket;
    }
}

