package com.qtra.scanner.dto;

import com.qtra.scanner.enums.QuantumSafetyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuantumReadinessResult {
    private String domain;
    private QuantumSafetyLevel safetyLevel;
    private double cipherStrengthScore;
    private double tlsVersionScore;
    private double pqcCertificateScore;
    private double hstsScore;
    private double dnssecScore;
    private double totalReadinessScore;
}