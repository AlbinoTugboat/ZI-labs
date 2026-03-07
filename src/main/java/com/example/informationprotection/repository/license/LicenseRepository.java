package com.example.informationprotection.repository.license;

import com.example.informationprotection.entity.license.Device;
import com.example.informationprotection.entity.license.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByCode(String code);

    @Query("""
            SELECT l
            FROM License l
            JOIN DeviceLicense dl ON dl.license = l
            WHERE dl.device = :device
              AND l.user.id = :userId
              AND l.product.id = :productId
              AND l.blocked = false
              AND l.product.isBlocked = false
              AND l.endingDate > :now
            """)
    Optional<License> findActiveByDeviceUserAndProduct(
            @Param("device") Device device,
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("now") LocalDateTime now
    );
}
