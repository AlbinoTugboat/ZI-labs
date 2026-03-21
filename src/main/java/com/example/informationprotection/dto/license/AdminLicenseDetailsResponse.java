package com.example.informationprotection.dto.license;

import java.time.LocalDateTime;
import java.util.List;

public class AdminLicenseDetailsResponse {
    private final Long id;
    private final String code;
    private final Long ownerId;
    private final String ownerUsername;
    private final Long userId;
    private final String username;
    private final Long productId;
    private final String productName;
    private final Long typeId;
    private final String typeName;
    private final Integer typeDefaultDurationInDays;
    private final Integer deviceCount;
    private final LocalDateTime firstActivationDate;
    private final LocalDateTime endingDate;
    private final boolean blocked;
    private final String description;
    private final List<ActivatedDeviceResponse> activatedDevices;

    public AdminLicenseDetailsResponse(
            Long id,
            String code,
            Long ownerId,
            String ownerUsername,
            Long userId,
            String username,
            Long productId,
            String productName,
            Long typeId,
            String typeName,
            Integer typeDefaultDurationInDays,
            Integer deviceCount,
            LocalDateTime firstActivationDate,
            LocalDateTime endingDate,
            boolean blocked,
            String description,
            List<ActivatedDeviceResponse> activatedDevices
    ) {
        this.id = id;
        this.code = code;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.userId = userId;
        this.username = username;
        this.productId = productId;
        this.productName = productName;
        this.typeId = typeId;
        this.typeName = typeName;
        this.typeDefaultDurationInDays = typeDefaultDurationInDays;
        this.deviceCount = deviceCount;
        this.firstActivationDate = firstActivationDate;
        this.endingDate = endingDate;
        this.blocked = blocked;
        this.description = description;
        this.activatedDevices = activatedDevices;
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

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getTypeDefaultDurationInDays() {
        return typeDefaultDurationInDays;
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

    public List<ActivatedDeviceResponse> getActivatedDevices() {
        return activatedDevices;
    }
}
