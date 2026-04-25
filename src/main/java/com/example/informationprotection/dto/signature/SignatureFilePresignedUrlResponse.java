package com.example.informationprotection.dto.signature;

import java.time.Instant;
import java.util.UUID;

public class SignatureFilePresignedUrlResponse {

    private final UUID signatureId;
    private final String originalFileName;
    private final String url;
    private final Instant expiresAt;

    public SignatureFilePresignedUrlResponse(
            UUID signatureId,
            String originalFileName,
            String url,
            Instant expiresAt
    ) {
        this.signatureId = signatureId;
        this.originalFileName = originalFileName;
        this.url = url;
        this.expiresAt = expiresAt;
    }

    public UUID getSignatureId() {
        return signatureId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getUrl() {
        return url;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
