package com.qtra.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TLSGroupedScanResult {
    private String rootDomain;
    private List<TLSScanResult> subdomains;
}
