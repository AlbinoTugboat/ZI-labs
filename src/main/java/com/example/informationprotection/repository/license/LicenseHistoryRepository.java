package com.example.informationprotection.repository.license;

import com.example.informationprotection.entity.license.License;
import com.example.informationprotection.entity.license.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
    List<LicenseHistory> findByLicenseOrderByChangeDateDesc(License license);
}
