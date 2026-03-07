package com.example.informationprotection.dto.license;

import java.time.LocalDateTime;

public class Ticket {
    private final LocalDateTime serverDate;
    private final long ticketLifetimeSeconds;
    private final LocalDateTime licenseActivationDate;
    private final LocalDateTime licenseExpirationDate;
    private final Long userId;
    private final Long deviceId;
    private final boolean licenseBlocked;

    public Ticket(LocalDateTime serverDate,
                  long ticketLifetimeSeconds,
                  LocalDateTime licenseActivationDate,
                  LocalDateTime licenseExpirationDate,
                  Long userId,
                  Long deviceId,
                  boolean licenseBlocked) {
        this.serverDate = serverDate;
        this.ticketLifetimeSeconds = ticketLifetimeSeconds;
        this.licenseActivationDate = licenseActivationDate;
        this.licenseExpirationDate = licenseExpirationDate;
        this.userId = userId;
        this.deviceId = deviceId;
        this.licenseBlocked = licenseBlocked;
    }

    public LocalDateTime getServerDate() {
        return serverDate;
    }

    public long getTicketLifetimeSeconds() {
        return ticketLifetimeSeconds;
    }

    public LocalDateTime getLicenseActivationDate() {
        return licenseActivationDate;
    }

    public LocalDateTime getLicenseExpirationDate() {
        return licenseExpirationDate;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public boolean isLicenseBlocked() {
        return licenseBlocked;
    }
}
