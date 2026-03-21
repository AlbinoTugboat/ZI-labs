package com.example.informationprotection.dto.license;

import java.time.LocalDateTime;

public class ActivatedDeviceResponse {
    private final Long id;
    private final String name;
    private final String macAddress;
    private final Long userId;
    private final String username;
    private final LocalDateTime activationDate;

    public ActivatedDeviceResponse(
            Long id,
            String name,
            String macAddress,
            Long userId,
            String username,
            LocalDateTime activationDate
    ) {
        this.id = id;
        this.name = name;
        this.macAddress = macAddress;
        this.userId = userId;
        this.username = username;
        this.activationDate = activationDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getActivationDate() {
        return activationDate;
    }
}
