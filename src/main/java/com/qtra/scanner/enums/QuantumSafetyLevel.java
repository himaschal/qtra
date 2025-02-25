package com.qtra.scanner.enums;

public enum QuantumSafetyLevel {
    NOT_QUANTUM_SAFE,  // Legacy ciphers (RSA, ECDSA, etc.)
    PQR_BUT_NOT_QUANTUM_SAFE, // Strong classical crypto (AES-GCM, ChaCha20)
    TRULY_QUANTUM_SAFE  // Hybrid PQC ciphers (Kyber, Dilithium, etc.)
}