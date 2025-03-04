package com.qtra.scanner.dto;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Add this to allow Jackson to instantiate objects
public class TLSScanResult {
    private String domain;
    private String protocol;
    private String cipherSuite;
    private double riskScore;

    @JsonCreator // Explicitly tell Jackson how to deserialize
    public TLSScanResult(
            @JsonProperty("domain") String domain,
            @JsonProperty("protocol") String protocol,
            @JsonProperty("cipherSuite") String cipherSuite,
            @JsonProperty("riskScore") double riskScore
    ) {
        this.domain = domain;
        this.protocol = protocol;
        this.cipherSuite = cipherSuite;
        this.riskScore = riskScore;
    }
}
