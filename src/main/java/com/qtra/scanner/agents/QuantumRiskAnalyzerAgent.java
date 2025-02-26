package com.qtra.scanner.agents;

import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.enums.QuantumSafetyLevel;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public TLSScanResult process(TLSScanResult scanResult) {
        QuantumSafetyLevel safetyLevel = classifyCipher(scanResult.getCipherSuite());
        return new TLSScanResult(scanResult.getDomain(), scanResult.getProtocol(), scanResult.getCipherSuite(), safetyLevel);
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
}
