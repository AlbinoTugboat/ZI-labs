package com.example.informationprotection.dto.signature;

public class SignatureCertificateResponse {
    private final String keyAlias;
    private final String algorithm;
    private final String certificatePem;

    public SignatureCertificateResponse(String keyAlias, String algorithm, String certificatePem) {
        this.keyAlias = keyAlias;
        this.algorithm = algorithm;
        this.certificatePem = certificatePem;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getCertificatePem() {
        return certificatePem;
    }
}
