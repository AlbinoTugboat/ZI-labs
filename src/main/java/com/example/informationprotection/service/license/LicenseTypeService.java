package com.example.informationprotection.service.license;

import com.example.informationprotection.entity.license.LicenseType;
import com.example.informationprotection.exception.NotFoundException;
import com.example.informationprotection.repository.license.LicenseTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    public LicenseType getTypeOrFail(Long typeId) {
        return licenseTypeRepository.findById(typeId)
                .orElseThrow(() -> new NotFoundException("License type not found: " + typeId));
    }
}
