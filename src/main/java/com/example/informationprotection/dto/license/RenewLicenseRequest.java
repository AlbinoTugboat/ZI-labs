package com.example.informationprotection.dto.license;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RenewLicenseRequest {

    @NotNull
    private Long licenseId;

    @Min(1)
    private Integer additionalDays;

    public Long getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Long licenseId) {
        this.licenseId = licenseId;
    }

    public Integer getAdditionalDays() {
        return additionalDays;
    }

    public void setAdditionalDays(Integer additionalDays) {
        this.additionalDays = additionalDays;
    }
}
