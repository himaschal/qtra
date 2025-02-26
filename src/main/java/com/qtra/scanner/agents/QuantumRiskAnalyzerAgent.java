package com.qtra.scanner.agents;

import com.qtra.scanner.dto.QuantumReadinessResult;
import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.enums.QuantumSafetyLevel;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;


@Service
public class QuantumRiskAnalyzerAgent {

    private static final List<String> TRULY_QUANTUM_SAFE_CIPHERS = List.of(
            "TLS_KYBER768_WITH_AES_128_GCM_SHA256",
            "TLS_KYBER1024_WITH_AES_256_GCM_SHA384"
    );

    private static final List<String> PQR_BUT_NOT_QUANTUM_SAFE_CIPHERS = List.of(
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256"
    );

    public QuantumReadinessResult analyze(TLSScanResult scanResult, boolean hstsEnabled, boolean dnssecEnabled) {
        QuantumSafetyLevel safetyLevel = classifyCipher(scanResult.getCipherSuite());

        double cipherStrengthScore = calculateCipherStrengthScore(scanResult.getCipherSuite());
        double tlsVersionScore = calculateTLSVersionScore(scanResult.getProtocol());
        double pqcCertificateScore = calculatePQCCertificateScore(scanResult.getDomain());
        double hstsScore = hstsEnabled ? 10.0 : 0.0;
        double dnssecScore = dnssecEnabled ? 10.0 : 0.0;

        return new QuantumReadinessResult(scanResult.getDomain(), safetyLevel, cipherStrengthScore,
                tlsVersionScore, pqcCertificateScore, hstsScore, dnssecScore);
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
        if ("TLSv1.3".equals(protocol)) {
            return 20.0;
        } else if ("TLSv1.2".equals(protocol)) {
            return 10.0;
        }
        return 0.0;
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

    public boolean checkHSTS(String domain) {
        try {
            URL url = new URL("https://" + domain);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);

            String hstsHeader = connection.getHeaderField("Strict-Transport-Security");
            return hstsHeader != null && hstsHeader.contains("max-age");

        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkDNSSEC(String domain) {
        try {
            Lookup lookup = new Lookup(domain, Type.DNSKEY);
            lookup.run();

            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                Record[] records = lookup.getAnswers();
                for (Record record : records) {
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
