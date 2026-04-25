package com.example.informationprotection.service.signature;

import com.example.informationprotection.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class MinioStorageService {

    private static final String DEFAULT_FILE_NAME = "uploaded-file.bin";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final int DEFAULT_PRESIGNED_TTL_SECONDS = 900;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final Object bucketCreationLock = new Object();
    private volatile boolean bucketValidated = false;

    public MinioStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public StoredObject uploadSignatureFile(UUID signatureId, MultipartFile file) {
        requireConfigured();
        ensureBucketExists();

        String safeFileName = sanitizeFileName(file.getOriginalFilename());
        String objectKey = "signatures/" + signatureId + "/" + Instant.now().toEpochMilli() + "-" + safeFileName;
        String contentType = normalizeContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("MINIO_UPLOAD_ERROR: cannot upload source file", ex);
        }

        return new StoredObject(
                minioProperties.getBucket(),
                objectKey,
                safeFileName,
                contentType,
                file.getSize(),
                Instant.now()
        );
    }

    public String getPresignedGetUrl(String bucketName, String objectKey) {
        requireConfigured();
        int ttlSeconds = resolvePresignedTtlSeconds();
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(ttlSeconds)
                            .build()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("MINIO_PRESIGNED_URL_ERROR: cannot generate pre-signed URL", ex);
        }
    }

    public int getPresignedTtlSeconds() {
        return resolvePresignedTtlSeconds();
    }

    private void ensureBucketExists() {
        if (bucketValidated) {
            return;
        }
        synchronized (bucketCreationLock) {
            if (bucketValidated) {
                return;
            }
            try {
                String bucket = minioProperties.getBucket();
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
                bucketValidated = true;
            } catch (Exception ex) {
                throw new IllegalStateException("MINIO_BUCKET_ERROR: cannot prepare bucket", ex);
            }
        }
    }

    private String sanitizeFileName(String originalFileName) {
        String fileName = Objects.requireNonNullElse(originalFileName, DEFAULT_FILE_NAME);
        int unixSeparatorIndex = fileName.lastIndexOf('/');
        int windowsSeparatorIndex = fileName.lastIndexOf('\\');
        int separatorIndex = Math.max(unixSeparatorIndex, windowsSeparatorIndex);
        if (separatorIndex >= 0 && separatorIndex < fileName.length() - 1) {
            fileName = fileName.substring(separatorIndex + 1);
        }
        fileName = fileName.trim();
        if (fileName.isEmpty()) {
            return DEFAULT_FILE_NAME;
        }
        String sanitized = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        if (sanitized.isBlank()) {
            return DEFAULT_FILE_NAME;
        }
        if (sanitized.length() > 200) {
            return sanitized.substring(sanitized.length() - 200);
        }
        return sanitized;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_CONTENT_TYPE;
        }
        return contentType.trim();
    }

    private int resolvePresignedTtlSeconds() {
        Integer configuredValue = minioProperties.getPresignedTtlSeconds();
        if (configuredValue == null || configuredValue <= 0) {
            return DEFAULT_PRESIGNED_TTL_SECONDS;
        }
        return configuredValue;
    }

    private void requireConfigured() {
        if (isBlank(minioProperties.getEndpoint())) {
            throw new IllegalStateException("MINIO_CONFIG_ERROR: storage.minio.endpoint is required");
        }
        if (isBlank(minioProperties.getAccessKey())) {
            throw new IllegalStateException("MINIO_CONFIG_ERROR: storage.minio.access-key is required");
        }
        if (isBlank(minioProperties.getSecretKey())) {
            throw new IllegalStateException("MINIO_CONFIG_ERROR: storage.minio.secret-key is required");
        }
        if (isBlank(minioProperties.getBucket())) {
            throw new IllegalStateException("MINIO_CONFIG_ERROR: storage.minio.bucket is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record StoredObject(
            String bucketName,
            String objectKey,
            String originalFileName,
            String contentType,
            long sizeBytes,
            Instant uploadedAt
    ) {
    }
}
