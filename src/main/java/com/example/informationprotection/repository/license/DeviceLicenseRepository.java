package com.example.informationprotection.repository.license;

import com.example.informationprotection.entity.license.Device;
import com.example.informationprotection.entity.license.DeviceLicense;
import com.example.informationprotection.entity.license.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    long countByLicense(License license);

    boolean existsByLicenseAndDevice(License license, Device device);

    @Query("""
            SELECT dl
            FROM DeviceLicense dl
            JOIN FETCH dl.license l
            JOIN FETCH dl.device d
            JOIN FETCH d.user
            WHERE l.id IN :licenseIds
            ORDER BY l.id, dl.activationDate
            """)
    List<DeviceLicense> findAllByLicenseIdsWithDevice(@Param("licenseIds") Collection<Long> licenseIds);
}
