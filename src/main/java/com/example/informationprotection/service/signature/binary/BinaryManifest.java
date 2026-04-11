package com.example.informationprotection.service.signature.binary;

import java.util.List;

public record BinaryManifest(
        byte[] magic,
        int version,
        BinaryExportType exportType,
        long generatedAtEpochMillis,
        long sinceEpochMillis,
        long recordCount,
        byte[] dataSha256,
        List<BinaryManifestEntry> entries
) {
}
