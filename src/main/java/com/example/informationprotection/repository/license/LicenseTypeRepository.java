package com.example.informationprotection.repository.license;

import com.example.informationprotection.entity.license.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {
}
