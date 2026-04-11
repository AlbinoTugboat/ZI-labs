package com.example.informationprotection.service.license;

import com.example.informationprotection.config.SignatureProperties;
import com.example.informationprotection.service.signature.SignatureKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketSignatureServiceTest {

    @Test
    void signAndVerifyTicketPayload() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        SignatureKeyProvider keyProvider = mock(SignatureKeyProvider.class);
        when(keyProvider.getPrivateKey()).thenReturn(keyPair.getPrivate());
        when(keyProvider.getPublicKey()).thenReturn(keyPair.getPublic());

        SignatureProperties properties = new SignatureProperties();
        properties.setAlgorithm("SHA256withRSA");

        TicketSignatureService ticketSignatureService = new TicketSignatureService(keyProvider, properties);
        CanonicalizationService canonicalizationService = new CanonicalizationService(new ObjectMapper());

        Map<String, Object> payloadOrderOne = new LinkedHashMap<>();
        payloadOrderOne.put("server_date", "2026-03-21T12:00:00");
        payloadOrderOne.put("ticket_lifetime_seconds", 120L);
        payloadOrderOne.put("user_id", 2L);

        Map<String, Object> payloadOrderTwo = new LinkedHashMap<>();
        payloadOrderTwo.put("user_id", 2L);
        payloadOrderTwo.put("server_date", "2026-03-21T12:00:00");
        payloadOrderTwo.put("ticket_lifetime_seconds", 120L);

        byte[] canonicalOne = canonicalizationService.canonicalize(payloadOrderOne);
        byte[] canonicalTwo = canonicalizationService.canonicalize(payloadOrderTwo);
        assertArrayEquals(canonicalOne, canonicalTwo);

        byte[] rawSignature = ticketSignatureService.signRaw(canonicalOne);
        assertNotNull(rawSignature);

        String signature = ticketSignatureService.sign(canonicalOne);
        assertNotNull(signature);
        assertTrue(rawSignature.length > 0);
        assertArrayEquals(rawSignature, Base64.getDecoder().decode(signature));
        assertTrue(ticketSignatureService.verify(canonicalOne, signature));

        Map<String, Object> tamperedPayload = new LinkedHashMap<>(payloadOrderOne);
        tamperedPayload.put("ticket_lifetime_seconds", 121L);
        byte[] tamperedCanonical = canonicalizationService.canonicalize(tamperedPayload);

        assertFalse(ticketSignatureService.verify(tamperedCanonical, signature));
    }
}
