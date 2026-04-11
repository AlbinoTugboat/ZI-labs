package com.example.informationprotection.service.license;

import com.example.informationprotection.config.SignatureProperties;
import com.example.informationprotection.service.signature.SignatureKeyProvider;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Service
public class TicketSignatureService {

    private final SignatureKeyProvider signatureKeyProvider;
    private final SignatureProperties signatureProperties;

    public TicketSignatureService(SignatureKeyProvider signatureKeyProvider, SignatureProperties signatureProperties) {
        this.signatureKeyProvider = signatureKeyProvider;
        this.signatureProperties = signatureProperties;
    }

    public String sign(byte[] canonicalBytes) {
        return Base64.getEncoder().encodeToString(signRaw(canonicalBytes));
    }

    public byte[] signRaw(byte[] payloadBytes) {
        if (payloadBytes == null || payloadBytes.length == 0) {
            throw new IllegalArgumentException("SIGNATURE_INPUT_INVALID: canonical bytes are empty");
        }

        try {
            Signature signature = Signature.getInstance(signatureProperties.getAlgorithm());
            signature.initSign(signatureKeyProvider.getPrivateKey());
            signature.update(payloadBytes);
            return signature.sign();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SIGNATURE_CRYPTO_ERROR: ticket signing failed", ex);
        }
    }

    public boolean verify(byte[] canonicalBytes, String base64Signature) {
        if (canonicalBytes == null || canonicalBytes.length == 0) {
            throw new IllegalArgumentException("SIGNATURE_INPUT_INVALID: canonical bytes are empty");
        }
        if (base64Signature == null || base64Signature.isBlank()) {
            throw new IllegalArgumentException("SIGNATURE_INPUT_INVALID: signature is empty");
        }

        try {
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);
            Signature signature = Signature.getInstance(signatureProperties.getAlgorithm());
            signature.initVerify(signatureKeyProvider.getPublicKey());
            signature.update(canonicalBytes);
            return signature.verify(signatureBytes);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("SIGNATURE_CRYPTO_ERROR: ticket signature verification failed", ex);
        }
    }

    public X509Certificate getSigningCertificate() {
        return signatureKeyProvider.getCertificate();
    }

    public String getSigningKeyAlias() {
        return signatureKeyProvider.getKeyAlias();
    }
}
