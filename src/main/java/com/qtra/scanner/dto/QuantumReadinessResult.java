package com.qtra.scanner.dto;

import com.qtra.scanner.enums.QuantumSafetyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuantumReadinessResult {
    private String domain;
    private QuantumSafetyLevel safetyLevel;
    private double cipherStrengthScore;
    private double tlsVersionScore;
    private double pqcCertificateScore;
    private double hstsScore;
    private double dnssecScore;
    private double totalReadinessScore;

    public QuantumReadinessResult(String domain, QuantumSafetyLevel safetyLevel,
                                  double cipherStrengthScore, double tlsVersionScore,
                                  double pqcCertificateScore, double hstsScore,
                                  double dnssecScore) {
        this(domain, safetyLevel, cipherStrengthScore, tlsVersionScore, pqcCertificateScore, hstsScore, dnssecScore, 0.0);
        this.totalReadinessScore = cipherStrengthScore + tlsVersionScore + pqcCertificateScore + hstsScore + dnssecScore;
    }

}
