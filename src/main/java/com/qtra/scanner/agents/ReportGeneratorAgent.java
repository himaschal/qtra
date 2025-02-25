package com.qtra.scanner.agents;

import org.springframework.stereotype.Service;

@Service
public class ReportGeneratorAgent implements AiAgent {

    @Override
    public String process(String input) {
        if (input.startsWith("Generated report for: ")) {
            return input;
        }
        return "Generated report for: " + input;
    }
}
