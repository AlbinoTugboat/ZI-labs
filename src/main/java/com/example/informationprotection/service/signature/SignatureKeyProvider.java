package com.example.informationprotection.service.signature;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public interface SignatureKeyProvider {
    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    X509Certificate getCertificate();

    String getKeyAlias();
}
