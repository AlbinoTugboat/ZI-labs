package com.example.informationprotection.repository.license;

import com.example.informationprotection.entity.license.Device;
import com.example.informationprotection.entity.license.DeviceLicense;
import com.example.informationprotection.entity.license.License;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    long countByLicense(License license);

    boolean existsByLicenseAndDevice(License license, Device device);
}
