package com.example.informationprotection.service.license;

import com.example.informationprotection.config.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TicketSignatureService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private final byte[] secret;

    public TicketSignatureService(
            @Value("${license.ticket-secret:}") String ticketSecret,
            JwtProperties jwtProperties
    ) {
        String effectiveSecret = (ticketSecret == null || ticketSecret.isBlank())
                ? jwtProperties.getSecret()
                : ticketSecret;
        this.secret = effectiveSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String sign(byte[] canonicalBytes) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret, HMAC_SHA256));
            byte[] signatureBytes = mac.doFinal(canonicalBytes);
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception ex) {
            throw new IllegalArgumentException("CANONICALIZATION_FAILED: ticket signing failed");
        }
    }
}
