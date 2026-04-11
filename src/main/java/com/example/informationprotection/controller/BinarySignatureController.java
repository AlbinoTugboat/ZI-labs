package com.example.informationprotection.controller;

import com.example.informationprotection.dto.signature.MalwareSignatureIdsRequest;
import com.example.informationprotection.service.signature.binary.BinarySignaturePackage;
import com.example.informationprotection.service.signature.binary.BinarySignaturePackageService;
import com.example.informationprotection.service.signature.binary.MultipartMixedResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/binary/signatures")
public class BinarySignatureController {

    private final BinarySignaturePackageService binarySignaturePackageService;
    private final MultipartMixedResponseFactory multipartMixedResponseFactory;

    public BinarySignatureController(
            BinarySignaturePackageService binarySignaturePackageService,
            MultipartMixedResponseFactory multipartMixedResponseFactory
    ) {
        this.binarySignaturePackageService = binarySignaturePackageService;
        this.multipartMixedResponseFactory = multipartMixedResponseFactory;
    }

    @GetMapping(value = "/full", produces = MediaType.MULTIPART_MIXED_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> getFullBase() {
        BinarySignaturePackage binaryPackage = binarySignaturePackageService.buildFullBasePackage();
        return multipartMixedResponseFactory.build(binaryPackage);
    }

    @GetMapping(value = "/increment", produces = MediaType.MULTIPART_MIXED_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> getIncrement(@RequestParam("since") Instant since) {
        BinarySignaturePackage binaryPackage = binarySignaturePackageService.buildIncrementPackage(since);
        return multipartMixedResponseFactory.build(binaryPackage);
    }

    @PostMapping(value = "/by-ids", produces = MediaType.MULTIPART_MIXED_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> getByIds(@Valid @RequestBody MalwareSignatureIdsRequest request) {
        BinarySignaturePackage binaryPackage = binarySignaturePackageService.buildByIdsPackage(request.getIds());
        return multipartMixedResponseFactory.build(binaryPackage);
    }
}
