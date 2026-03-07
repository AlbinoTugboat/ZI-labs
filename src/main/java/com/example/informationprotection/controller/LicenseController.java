package com.example.informationprotection.controller;

import com.example.informationprotection.dto.license.ActivateLicenseRequest;
import com.example.informationprotection.dto.license.CheckLicenseRequest;
import com.example.informationprotection.dto.license.CreateLicenseRequest;
import com.example.informationprotection.dto.license.LicenseResponse;
import com.example.informationprotection.dto.license.LicenseTicketResponse;
import com.example.informationprotection.service.license.ApplicationUserService;
import com.example.informationprotection.service.license.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LicenseController {

    private final LicenseService licenseService;
    private final ApplicationUserService applicationUserService;

    public LicenseController(LicenseService licenseService, ApplicationUserService applicationUserService) {
        this.licenseService = licenseService;
        this.applicationUserService = applicationUserService;
    }

    @PostMapping("/admin/licenses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> createLicense(
            @Valid @RequestBody CreateLicenseRequest request,
            Authentication authentication
    ) {
        Long adminId = applicationUserService.resolveUserId(authentication);
        LicenseResponse response = licenseService.createLicense(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/user/licenses/check")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LicenseTicketResponse> checkLicense(
            @Valid @RequestBody CheckLicenseRequest request,
            Authentication authentication
    ) {
        Long userId = applicationUserService.resolveUserId(authentication);
        LicenseTicketResponse response = licenseService.checkLicense(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/licenses/activate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LicenseTicketResponse> activateLicense(
            @Valid @RequestBody ActivateLicenseRequest request,
            Authentication authentication
    ) {
        Long userId = applicationUserService.resolveUserId(authentication);
        LicenseTicketResponse response = licenseService.activateLicense(request, userId);
        return ResponseEntity.ok(response);
    }
}
