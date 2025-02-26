package com.qtra.scanner.agents;

import com.qtra.scanner.dto.QuantumReadinessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReportGeneratorAgent {

    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorAgent.class);

    /**
     * Processes QuantumReadinessResult and generates a simple report.
     * @param readinessResult The result containing TLS security scores.
     */
    public void generateReport(QuantumReadinessResult readinessResult) {
        StringBuilder report = new StringBuilder();
        report.append("\n📌 Quantum Readiness Report for ").append(readinessResult.getDomain()).append("\n");
        report.append("--------------------------------------------------\n");
        report.append("🔹 Quantum Safety Level: ").append(readinessResult.getSafetyLevel()).append("\n");
        report.append("🔹 Cipher Strength Score: ").append(readinessResult.getCipherStrengthScore()).append("/40\n");
        report.append("🔹 TLS Version Score: ").append(readinessResult.getTlsVersionScore()).append("/20\n");
        report.append("🔹 PQC Certificate Score: ").append(readinessResult.getPqcCertificateScore()).append("/20\n");
        report.append("🔹 HSTS Score: ").append(readinessResult.getHstsScore()).append("/10\n");
        report.append("🔹 DNSSEC Score: ").append(readinessResult.getDnssecScore()).append("/10\n");
        report.append("--------------------------------------------------\n");
        report.append("✅ **Total Quantum Readiness Score: ").append(readinessResult.getTotalReadinessScore()).append("/100**\n");

        logger.info(report.toString());
    }
}
