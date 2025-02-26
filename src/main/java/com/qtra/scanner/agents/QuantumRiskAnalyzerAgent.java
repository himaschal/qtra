package com.qtra.scanner.agents;

import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.enums.QuantumSafetyLevel;
import org.springframework.stereotype.Service;

@Service
public class QuantumRiskAnalyzerAgent {

    public String process(TLSScanResult scanResult) {
        String domain = scanResult.getDomain();
        QuantumSafetyLevel safetyLevel = scanResult.getSafetyLevel();

        String analysisResult;
        switch (safetyLevel) {
            case TRULY_QUANTUM_SAFE:
                analysisResult = "TLS configuration for " + domain + " is **Quantum-Safe** ✅";
                break;
            case PQR_BUT_NOT_QUANTUM_SAFE:
                analysisResult = "TLS configuration for " + domain + " uses **PQC-Ready Ciphers**, but not fully quantum-safe ⚠️";
                break;
            default:
                analysisResult = "TLS configuration for " + domain + " is **Not Quantum-Safe** ❌";
                break;
        }

        System.out.println("Quantum Risk Analysis for: " + analysisResult);
        return analysisResult;
    }
}
