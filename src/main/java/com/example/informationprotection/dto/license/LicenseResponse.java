package com.example.informationprotection.dto.license;

import java.time.LocalDateTime;

public class LicenseResponse {
    private final Long id;
    private final String code;
    private final Long ownerId;
    private final Long productId;
    private final Long typeId;
    private final Integer deviceCount;
    private final LocalDateTime firstActivationDate;
    private final LocalDateTime endingDate;
    private final boolean blocked;
    private final String description;

    public LicenseResponse(Long id,
                           String code,
                           Long ownerId,
                           Long productId,
                           Long typeId,
                           Integer deviceCount,
                           LocalDateTime firstActivationDate,
                           LocalDateTime endingDate,
                           boolean blocked,
                           String description) {
        this.id = id;
        this.code = code;
        this.ownerId = ownerId;
        this.productId = productId;
        this.typeId = typeId;
        this.deviceCount = deviceCount;
        this.firstActivationDate = firstActivationDate;
        this.endingDate = endingDate;
        this.blocked = blocked;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public Integer getDeviceCount() {
        return deviceCount;
    }

    public LocalDateTime getFirstActivationDate() {
        return firstActivationDate;
    }

    public LocalDateTime getEndingDate() {
        return endingDate;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getDescription() {
        return description;
    }
}
