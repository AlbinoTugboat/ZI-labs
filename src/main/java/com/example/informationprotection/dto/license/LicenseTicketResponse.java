package com.example.informationprotection.dto.license;

import java.time.LocalDateTime;

public class LicenseTicketResponse {
    private Long licenseId;
    private Long productId;
    private LocalDateTime expiration;
    private String signature;

    public LicenseTicketResponse(Long licenseId, Long productId, LocalDateTime expiration, String signature) {
        this.licenseId = licenseId;
        this.productId = productId;
        this.expiration = expiration;
        this.signature = signature;
    }

    public Long getLicenseId() {
        return licenseId;
    }

    public Long getProductId() {
        return productId;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public String getSignature() {
        return signature;
    }
}
