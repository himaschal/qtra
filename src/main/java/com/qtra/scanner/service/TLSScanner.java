package com.qtra.scanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtra.scanner.enums.QuantumSafetyLevel;
import com.qtra.scanner.dto.TLSScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TLSScanner {

    private static final Logger logger = LoggerFactory.getLogger(TLSScanner.class);
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    public TLSScanner(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Scans a domain and its subdomains asynchronously, then publishes results to Kafka.
     * @param domain The primary domain to scan.
     */
    public void scanAndPublish(String domain) {
        CompletableFuture<List<TLSScanResult>> futureResults = scanWithSubdomains(domain);
        futureResults.thenAccept(results -> {
            try {
                String jsonResults = objectMapper.writeValueAsString(results);
                kafkaProducerService.sendMessage("tls-scan-results", domain, jsonResults);
            } catch (Exception e) {
                logger.error("Error publishing TLS scan results: ", e);
            }
        });
    }

    /**
     * Scans a domain and its subdomains asynchronously.
     * @param domain The primary domain.
     * @return A CompletableFuture containing the list of TLS scan results.
     */
    public CompletableFuture<List<TLSScanResult>> scanWithSubdomains(String domain) {
        List<CompletableFuture<TLSScanResult>> futures = new ArrayList<>();
        List<String> subdomains = discoverSubdomains(domain);

        for (String subdomain : subdomains) {
            futures.add(CompletableFuture.supplyAsync(() -> scan(subdomain)));
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

    /**
     * Performs a TLS scan on a single domain.
     * @param domain The domain to scan.
     * @return A TLSScanResult containing the protocol and cipher suite.
     */
    private TLSScanResult scan(String domain) {
        try {
            SSLSocket sslSocket = createSSLSocket(domain, 443);
            SSLSession session = sslSocket.getSession();

            String protocol = session.getProtocol();
            String cipherSuite = session.getCipherSuite();
            sslSocket.close();

            logger.info("✅ Scanned {}: Protocol={}, Cipher={}", domain, protocol, cipherSuite);
            return new TLSScanResult(domain, protocol, cipherSuite);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to scan TLS for {}: {}", domain, e.getMessage());
            return new TLSScanResult(domain, "UNKNOWN", "UNKNOWN");
        }
    }

    /**
     * Creates an SSLSocket to establish a TLS connection and retrieve SSL details.
     * @param domain The domain to scan.
     * @param port The port to connect to (usually 443).
     * @return An SSLSocket with an active TLS session.
     * @throws Exception If the connection fails.
     */
    private SSLSocket createSSLSocket(String domain, int port) throws Exception {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(domain, port);
        socket.setEnabledProtocols(socket.getSupportedProtocols());
        socket.startHandshake();
        return socket;
    }

    /**
     * Discovers common subdomains for a given domain.
     * @param domain The root domain.
     * @return A list of subdomains to scan.
     */
    private List<String> discoverSubdomains(String domain) {
        List<String> subdomains = new ArrayList<>();
        String[] commonSubdomains = {"www", "api", "mail", "blog", "shop"};

        for (String sub : commonSubdomains) {
            String fullDomain = sub + "." + domain;
            if (isDomainActive(fullDomain)) {
                subdomains.add(fullDomain);
            }
        }

        // Always scan the root domain
        subdomains.add(domain);
        logger.info("🔍 Discovered subdomains for {}: {}", domain, subdomains);
        return subdomains;
    }

    /**
     * Checks if a domain is active by resolving its DNS.
     * @param domain The domain to check.
     * @return True if the domain resolves, false otherwise.
     */
    private boolean isDomainActive(String domain) {
        try {
            InetAddress.getByName(domain);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

