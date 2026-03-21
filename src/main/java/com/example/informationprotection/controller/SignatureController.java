package com.example.informationprotection.controller;

import com.example.informationprotection.config.SignatureProperties;
import com.example.informationprotection.dto.signature.SignatureCertificateResponse;
import com.example.informationprotection.service.license.TicketSignatureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;
import java.util.Base64;

@RestController
@RequestMapping("/api/public/signature")
public class SignatureController {

    private final TicketSignatureService ticketSignatureService;
    private final SignatureProperties signatureProperties;

    public SignatureController(TicketSignatureService ticketSignatureService, SignatureProperties signatureProperties) {
        this.ticketSignatureService = ticketSignatureService;
        this.signatureProperties = signatureProperties;
    }

    @GetMapping("/certificate")
    public SignatureCertificateResponse getCertificate() {
        X509Certificate certificate = ticketSignatureService.getSigningCertificate();
        return new SignatureCertificateResponse(
                ticketSignatureService.getSigningKeyAlias(),
                signatureProperties.getAlgorithm(),
                toPem(certificate)
        );
    }

    private String toPem(X509Certificate certificate) {
        try {
            String encoded = Base64.getMimeEncoder(64, "\n".getBytes())
                    .encodeToString(certificate.getEncoded());
            return "-----BEGIN CERTIFICATE-----\n" + encoded + "\n-----END CERTIFICATE-----";
        } catch (Exception ex) {
            throw new IllegalStateException("SIGNATURE_CRYPTO_ERROR: cannot export certificate", ex);
        }
    }
}
