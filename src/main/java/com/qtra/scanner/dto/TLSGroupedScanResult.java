package com.qtra.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructorsrc/main/java/com/qtra/scanner/dto/TLSGroupedScanResult.java
public class TLSGroupedScanResult {
    private String rootDomain;
    private List<TLSScanResult> subdomains;
}
