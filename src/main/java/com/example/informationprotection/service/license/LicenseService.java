package com.example.informationprotection.service.license;

import com.example.informationprotection.dto.license.ActivateLicenseRequest;
import com.example.informationprotection.dto.license.ActivatedDeviceResponse;
import com.example.informationprotection.dto.license.AdminLicenseDetailsResponse;
import com.example.informationprotection.dto.license.CheckLicenseRequest;
import com.example.informationprotection.dto.license.CreateLicenseRequest;
import com.example.informationprotection.dto.license.LicenseResponse;
import com.example.informationprotection.dto.license.LicenseTicketResponse;
import com.example.informationprotection.dto.license.RenewLicenseRequest;
import com.example.informationprotection.dto.license.Ticket;
import com.example.informationprotection.entity.User;
import com.example.informationprotection.entity.license.Device;
import com.example.informationprotection.entity.license.DeviceLicense;
import com.example.informationprotection.entity.license.License;
import com.example.informationprotection.entity.license.LicenseHistory;
import com.example.informationprotection.entity.license.LicenseHistoryStatus;
import com.example.informationprotection.entity.license.LicenseType;
import com.example.informationprotection.entity.license.Product;
import com.example.informationprotection.exception.ConflictException;
import com.example.informationprotection.exception.ForbiddenOperationException;
import com.example.informationprotection.exception.NotFoundException;
import com.example.informationprotection.repository.license.DeviceLicenseRepository;
import com.example.informationprotection.repository.license.DeviceRepository;
import com.example.informationprotection.repository.license.LicenseHistoryRepository;
import com.example.informationprotection.repository.license.LicenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final ApplicationUserService applicationUserService;
    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final CanonicalizationService canonicalizationService;
    private final TicketSignatureService ticketSignatureService;

    public LicenseService(
            LicenseRepository licenseRepository,
            DeviceRepository deviceRepository,
            DeviceLicenseRepository deviceLicenseRepository,
            LicenseHistoryRepository licenseHistoryRepository,
            ApplicationUserService applicationUserService,
            ProductService productService,
            LicenseTypeService licenseTypeService,
            CanonicalizationService canonicalizationService,
            TicketSignatureService ticketSignatureService
    ) {
        this.licenseRepository = licenseRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
        this.applicationUserService = applicationUserService;
        this.productService = productService;
        this.licenseTypeService = licenseTypeService;
        this.canonicalizationService = canonicalizationService;
        this.ticketSignatureService = ticketSignatureService;
    }

    @Transactional
    public LicenseResponse createLicense(CreateLicenseRequest request, Long adminId) {
        User admin = applicationUserService.getActiveUserOrFail(adminId);
        Product product = productService.getProductOrFail(request.getProductId());
        LicenseType licenseType = licenseTypeService.getTypeOrFail(request.getTypeId());
        User ownerUser = applicationUserService.getActiveUserOrFail(request.getOwnerId());

        if (product.isBlocked()) {
            throw new ConflictException("Product is blocked");
        }

        if (request.getDeviceCount() == null || request.getDeviceCount() < 1) {
            throw new IllegalArgumentException("deviceCount must be >= 1");
        }

        License license = new License();
        license.setCode(generateCode());
        license.setOwner(ownerUser);
        license.setUser(null);
        license.setProduct(product);
        license.setType(licenseType);
        license.setBlocked(false);
        license.setDeviceCount(request.getDeviceCount());
        license.setDescription(request.getDescription());

        License saved = licenseRepository.save(license);
        saveHistory(saved, admin, LicenseHistoryStatus.CREATED, "License created");

        return toResponse(saved);
    }

    @Transactional
    public LicenseResponse renewLicense(RenewLicenseRequest request, Long adminId) {
        User admin = applicationUserService.getActiveUserOrFail(adminId);
        License license = licenseRepository.findById(request.getLicenseId())
                .orElseThrow(() -> new NotFoundException("License not found"));

        if (license.isBlocked()) {
            throw new ConflictException("License is blocked");
        }

        if (license.getProduct().isBlocked()) {
            throw new ConflictException("Product is blocked");
        }

        Integer defaultDuration = license.getType().getDefaultDurationInDays();
        if (defaultDuration == null || defaultDuration < 1) {
            throw new IllegalArgumentException("License type duration must be > 0");
        }

        int additionalDays = request.getAdditionalDays() == null
                ? defaultDuration
                : request.getAdditionalDays();

        if (additionalDays < 1) {
            throw new IllegalArgumentException("additionalDays must be >= 1");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentEndingDate = license.getEndingDate();
        LocalDateTime baseDate = (currentEndingDate != null && currentEndingDate.isAfter(now))
                ? currentEndingDate
                : now;

        license.setEndingDate(baseDate.plusDays(additionalDays));
        License saved = licenseRepository.save(license);

        saveHistory(saved, admin, LicenseHistoryStatus.RENEWED, "License renewed for " + additionalDays + " day(s)");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LicenseTicketResponse checkLicense(CheckLicenseRequest request, Long userId) {
        User user = applicationUserService.getActiveUserOrFail(userId);
        String normalizedMac = normalizeMac(request.getDeviceMac());

        Device device = deviceRepository.findByMacAddress(normalizedMac)
                .orElseThrow(() -> new NotFoundException("Device not found"));

        if (!device.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Device not found");
        }

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                        device,
                        user.getId(),
                        request.getProductId(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new NotFoundException("License not found"));

        validateLicenseRules(license, user);

        return buildTicket(license, device);
    }

    @Transactional
    public LicenseTicketResponse activateLicense(ActivateLicenseRequest request, Long userId) {
        User user = applicationUserService.getActiveUserOrFail(userId);

        License license = licenseRepository.findByCode(request.getActivationKey().trim())
                .orElseThrow(() -> new NotFoundException("License not found"));

        if (license.isBlocked()) {
            throw new ConflictException("License is blocked");
        }

        if (license.getProduct().isBlocked()) {
            throw new ConflictException("Product is blocked");
        }

        if (license.getUser() != null && !license.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("License already owned by another user");
        }

        String normalizedMac = normalizeMac(request.getDeviceMac());
        Device device = deviceRepository.findByMacAddress(normalizedMac)
                .map(existing -> ensureDeviceOwnership(existing, user))
                .orElseGet(() -> createDevice(user, request.getDeviceName(), normalizedMac));

        LocalDateTime now = LocalDateTime.now();

        if (license.getUser() == null) {
            activateFirstTime(license, user, device, now);
        } else {
            activateOnAdditionalDevice(license, user, device, now);
        }

        validateLicenseRules(license, user);
        return buildTicket(license, device);
    }

    @Transactional(readOnly = true)
    public List<AdminLicenseDetailsResponse> getAllLicensesWithDevices() {
        List<License> licenses = licenseRepository.findAllForAdminView();
        if (licenses.isEmpty()) {
            return List.of();
        }

        List<Long> licenseIds = licenses.stream()
                .map(License::getId)
                .toList();

        List<DeviceLicense> allDeviceLicenses = deviceLicenseRepository.findAllByLicenseIdsWithDevice(licenseIds);
        Map<Long, List<DeviceLicense>> deviceLicensesByLicenseId = new LinkedHashMap<>();
        for (DeviceLicense deviceLicense : allDeviceLicenses) {
            Long licenseId = deviceLicense.getLicense().getId();
            deviceLicensesByLicenseId
                    .computeIfAbsent(licenseId, ignored -> new ArrayList<>())
                    .add(deviceLicense);
        }

        List<AdminLicenseDetailsResponse> response = new ArrayList<>(licenses.size());
        for (License license : licenses) {
            List<DeviceLicense> linkedDevices = deviceLicensesByLicenseId.getOrDefault(license.getId(), List.of());
            response.add(toAdminDetailsResponse(license, linkedDevices));
        }

        return response;
    }

    private void activateFirstTime(License license, User user, Device device, LocalDateTime now) {
        int durationInDays = license.getType().getDefaultDurationInDays() == null
                ? 0
                : license.getType().getDefaultDurationInDays();

        if (durationInDays < 1) {
            throw new IllegalArgumentException("License type duration must be > 0");
        }

        license.setUser(user);
        license.setFirstActivationDate(now);
        license.setEndingDate(now.plusDays(durationInDays));
        licenseRepository.save(license);

        linkDevice(license, device, now);
        saveHistory(license, user, LicenseHistoryStatus.ACTIVATED, "First activation");
    }

    private void activateOnAdditionalDevice(License license, User user, Device device, LocalDateTime now) {
        boolean alreadyLinked = deviceLicenseRepository.existsByLicenseAndDevice(license, device);

        if (!alreadyLinked) {
            long currentDevices = deviceLicenseRepository.countByLicense(license);
            if (currentDevices >= license.getDeviceCount()) {
                throw new ConflictException("Device limit reached");
            }
            linkDevice(license, device, now);
        }

        saveHistory(license, user, LicenseHistoryStatus.ACTIVATED, "Activation");
    }

    private Device ensureDeviceOwnership(Device device, User user) {
        if (!device.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Device belongs to another user");
        }
        return device;
    }

    private Device createDevice(User user, String deviceName, String normalizedMac) {
        Device device = new Device();
        device.setUser(user);
        device.setName(deviceName.trim());
        device.setMacAddress(normalizedMac);
        return deviceRepository.save(device);
    }

    private void linkDevice(License license, Device device, LocalDateTime now) {
        DeviceLicense deviceLicense = new DeviceLicense();
        deviceLicense.setLicense(license);
        deviceLicense.setDevice(device);
        deviceLicense.setActivationDate(now);
        deviceLicenseRepository.save(deviceLicense);
    }

    private void saveHistory(License license, User user, LicenseHistoryStatus status, String description) {
        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(user);
        history.setStatus(status);
        history.setChangeDate(LocalDateTime.now());
        history.setDescription(description);
        licenseHistoryRepository.save(history);
    }

    private void validateLicenseRules(License license, User user) {
        if (license.isBlocked()) {
            throw new ConflictException("License is blocked");
        }

        if (license.getProduct().isBlocked()) {
            throw new ConflictException("Product is blocked");
        }

        if (license.getEndingDate() == null || !license.getEndingDate().isAfter(LocalDateTime.now())) {
            throw new ConflictException("License expired");
        }

        long linkedDevices = deviceLicenseRepository.countByLicense(license);
        if (linkedDevices > license.getDeviceCount()) {
            throw new ConflictException("Device count exceeded");
        }

        applicationUserService.ensureAccountActive(user);
    }

    private LicenseTicketResponse buildTicket(License license, Device device) {
        if (license.getFirstActivationDate() == null) {
            throw new ConflictException("License activation date is missing");
        }

        LocalDateTime serverDate = LocalDateTime.now();
        long ticketLifetimeSeconds = Math.max(0, Duration.between(serverDate, license.getEndingDate()).getSeconds());

        Ticket ticket = new Ticket(
                serverDate,
                ticketLifetimeSeconds,
                license.getFirstActivationDate(),
                license.getEndingDate(),
                license.getUser().getId(),
                device.getId(),
                license.isBlocked()
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("server_date", ticket.getServerDate().toString());
        payload.put("ticket_lifetime_seconds", ticket.getTicketLifetimeSeconds());
        payload.put("license_activation_date", ticket.getLicenseActivationDate().toString());
        payload.put("license_expiration_date", ticket.getLicenseExpirationDate().toString());
        payload.put("user_id", ticket.getUserId());
        payload.put("device_id", ticket.getDeviceId());
        payload.put("license_blocked", ticket.isLicenseBlocked());

        byte[] canonicalBytes = canonicalizationService.canonicalize(payload);
        String signature = ticketSignatureService.sign(canonicalBytes);

        return new LicenseTicketResponse(ticket, signature);
    }

    private LicenseResponse toResponse(License license) {
        return new LicenseResponse(
                license.getId(),
                license.getCode(),
                license.getOwner().getId(),
                license.getProduct().getId(),
                license.getType().getId(),
                license.getDeviceCount(),
                license.getFirstActivationDate(),
                license.getEndingDate(),
                license.isBlocked(),
                license.getDescription()
        );
    }

    private AdminLicenseDetailsResponse toAdminDetailsResponse(License license, List<DeviceLicense> deviceLicenses) {
        Long userId = license.getUser() == null ? null : license.getUser().getId();
        String username = license.getUser() == null ? null : license.getUser().getUsername();

        List<ActivatedDeviceResponse> devices = new ArrayList<>(deviceLicenses.size());
        for (DeviceLicense deviceLicense : deviceLicenses) {
            Device device = deviceLicense.getDevice();
            devices.add(new ActivatedDeviceResponse(
                    device.getId(),
                    device.getName(),
                    device.getMacAddress(),
                    device.getUser().getId(),
                    device.getUser().getUsername(),
                    deviceLicense.getActivationDate()
            ));
        }

        return new AdminLicenseDetailsResponse(
                license.getId(),
                license.getCode(),
                license.getOwner().getId(),
                license.getOwner().getUsername(),
                userId,
                username,
                license.getProduct().getId(),
                license.getProduct().getName(),
                license.getType().getId(),
                license.getType().getName(),
                license.getType().getDefaultDurationInDays(),
                license.getDeviceCount(),
                license.getFirstActivationDate(),
                license.getEndingDate(),
                license.isBlocked(),
                license.getDescription(),
                devices
        );
    }

    private String generateCode() {
        String code;
        do {
            code = "LIC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        } while (licenseRepository.findByCode(code).isPresent());
        return code;
    }

    private String normalizeMac(String rawMac) {
        if (rawMac == null || rawMac.isBlank()) {
            throw new IllegalArgumentException("deviceMac is required");
        }

        String normalized = rawMac.trim().toUpperCase();
        if (!normalized.matches("^[0-9A-F]{2}([-:][0-9A-F]{2}){5}$")) {
            throw new IllegalArgumentException("Invalid MAC address format");
        }

        return normalized.replace('-', ':');
    }
}
