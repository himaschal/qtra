package com.qtra.scanner.service;

import com.qtra.scanner.dto.TLSScanResult;
import com.qtra.scanner.enums.QuantumSafetyLevel;
import org.springframework.stereotype.Service;

@Service
public class SSLScannerAgent {

    public String analyze(TLSScanResult result) {
        System.out.println("Analyzing TLSScanResult for domain: " + result.getDomain());
        StringBuilder analysis = new StringBuilder();

        // Check Protocol Version
        if (result.getProtocol().equals("TLSv1.3")) {
            analysis.append("Protocol is secure: ").append(result.getProtocol()).append(".\n");
        } else if (result.getProtocol().equals("TLSv1.2")) {
            analysis.append("Protocol is acceptable: ").append(result.getProtocol()).append(".\n");
        } else {
            analysis.append("Warning: Insecure protocol version detected: ").append(result.getProtocol()).append(".\n");
        }

        // Check Cipher Suite Strength
        if (result.getSafetyLevel() == QuantumSafetyLevel.NOT_QUANTUM_SAFE) {
            analysis.append("Warning: Cipher suite is not quantum safe: ").append(result.getCipherSuite()).append(".\n");
        } else {
            analysis.append("Cipher suite is considered secure: ").append(result.getCipherSuite()).append(".\n");
        }

        // Certificate Validity Check (mock example; implement actual checks as needed)
        // This would typically require additional logic to check the certificate
        analysis.append("Check certificate validity: VALID (mock result).").append("\n");

        // HTTP Security Headers Check (mock example)
        analysis.append("Check for HTTP security headers: MISSING (mock result).").append("\n");

        // Vulnerability Check (mock example)
        analysis.append("Check against known vulnerabilities: NO ISSUES FOUND (mock result).").append("\n");

        System.out.println("Analysis completed for domain: " + result.getDomain());

        return analysis.toString();
    }
}
