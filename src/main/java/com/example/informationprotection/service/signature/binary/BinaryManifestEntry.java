package com.example.informationprotection.service.signature.binary;

import java.util.UUID;

public record BinaryManifestEntry(
        UUID id,
        int statusCode,
        long updatedAtEpochMillis,
        long dataOffset,
        long dataLength,
        byte[] recordSignatureBytes
) {
}
