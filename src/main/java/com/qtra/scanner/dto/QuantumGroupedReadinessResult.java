package com.qtra.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class QuantumGroupedReadinessResult {
    private String rootDomain;
    private List<QuantumReadinessResult> subdomainResults;
    private double averageReadinessScore;
}