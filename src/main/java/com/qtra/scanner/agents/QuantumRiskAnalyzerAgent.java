package com.qtra.scanner.agents;

import org.springframework.stereotype.Service;

@Service
public class QuantumRiskAnalyzerAgent implements AiAgent {

    @Override
    public String process(String input) {
        if (input.startsWith("Quantum Risk Analysis Result for: ")) {
            return input;
        }
        return "Quantum Risk Analysis Result for: " + input;
    }
}
