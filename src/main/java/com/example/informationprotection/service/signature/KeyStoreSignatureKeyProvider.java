package com.example.informationprotection.service.signature;

import com.example.informationprotection.config.SignatureProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

@Service
public class KeyStoreSignatureKeyProvider implements SignatureKeyProvider {

    private final SignatureProperties signatureProperties;
    private final ResourceLoader resourceLoader;

    private volatile LoadedKeyMaterial cachedKeyMaterial;

    public KeyStoreSignatureKeyProvider(SignatureProperties signatureProperties, ResourceLoader resourceLoader) {
        this.signatureProperties = signatureProperties;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return getOrLoad().privateKey();
    }

    @Override
    public PublicKey getPublicKey() {
        return getOrLoad().publicKey();
    }

    @Override
    public X509Certificate getCertificate() {
        return getOrLoad().certificate();
    }

    @Override
    public String getKeyAlias() {
        return getOrLoad().keyAlias();
    }

    private LoadedKeyMaterial getOrLoad() {
        LoadedKeyMaterial local = cachedKeyMaterial;
        if (local != null) {
            return local;
        }

        synchronized (this) {
            if (cachedKeyMaterial == null) {
                cachedKeyMaterial = loadKeyMaterial();
            }
            return cachedKeyMaterial;
        }
    }

    private LoadedKeyMaterial loadKeyMaterial() {
        String keyStorePath = requireNotBlank(signatureProperties.getKeyStorePath(),
                "SIGNATURE_CONFIG_ERROR: signature.key-store-path is required");
        String keyStoreType = requireNotBlank(signatureProperties.getKeyStoreType(),
                "SIGNATURE_CONFIG_ERROR: signature.key-store-type is required");
        String keyStorePassword = requireNotBlank(signatureProperties.getKeyStorePassword(),
                "SIGNATURE_CONFIG_ERROR: signature.key-store-password is required");

        try (InputStream inputStream = openResourceInputStream(keyStorePath)) {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            char[] storePassword = keyStorePassword.toCharArray();
            keyStore.load(inputStream, storePassword);

            String alias = resolveAlias(keyStore);
            char[] keyPassword = resolveKeyPassword(keyStorePassword);

            Key key = keyStore.getKey(alias, keyPassword);
            if (!(key instanceof PrivateKey privateKey)) {
                throw new IllegalStateException("SIGNATURE_CRYPTO_ERROR: key entry is not a private key");
            }

            Certificate certificate = keyStore.getCertificate(alias);
            if (!(certificate instanceof X509Certificate x509Certificate)) {
                throw new IllegalStateException("SIGNATURE_CRYPTO_ERROR: certificate must be X509");
            }

            return new LoadedKeyMaterial(alias, privateKey, x509Certificate.getPublicKey(), x509Certificate);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new IllegalStateException("SIGNATURE_KEYSTORE_ACCESS_ERROR: cannot access keystore", ex);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SIGNATURE_CRYPTO_ERROR: cannot load signature keys", ex);
        }
    }

    private InputStream openResourceInputStream(String keyStorePath) throws IOException {
        String normalizedPath = keyStorePath.startsWith("classpath:") || keyStorePath.startsWith("file:")
                ? keyStorePath
                : "file:" + keyStorePath;

        Resource resource = resourceLoader.getResource(normalizedPath);
        if (!resource.exists()) {
            throw new IllegalStateException("SIGNATURE_KEYSTORE_ACCESS_ERROR: keystore file not found");
        }
        return resource.getInputStream();
    }

    private String resolveAlias(KeyStore keyStore) throws GeneralSecurityException {
        String configuredAlias = signatureProperties.getKeyAlias();
        if (configuredAlias != null && !configuredAlias.isBlank()) {
            if (!keyStore.containsAlias(configuredAlias)) {
                throw new IllegalStateException("SIGNATURE_CONFIG_ERROR: signature.key-alias not found in keystore");
            }
            if (!keyStore.isKeyEntry(configuredAlias)) {
                throw new IllegalStateException("SIGNATURE_CONFIG_ERROR: signature.key-alias is not a key entry");
            }
            return configuredAlias;
        }

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                return alias;
            }
        }

        throw new IllegalStateException("SIGNATURE_CONFIG_ERROR: no private key entry found in keystore");
    }

    private char[] resolveKeyPassword(String keyStorePassword) {
        String keyPassword = signatureProperties.getKeyPassword();
        if (keyPassword == null || keyPassword.isBlank()) {
            return keyStorePassword.toCharArray();
        }
        return keyPassword.toCharArray();
    }

    private String requireNotBlank(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }
        return value;
    }

    private record LoadedKeyMaterial(
            String keyAlias,
            PrivateKey privateKey,
            PublicKey publicKey,
            X509Certificate certificate
    ) {
    }
}
