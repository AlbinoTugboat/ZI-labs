package com.example.informationprotection.service.signature.binary;

import org.springframework.stereotype.Component;

@Component
public class BinaryManifestSerializer {

    private static final int MAGIC_LENGTH = 12;
    private static final int SHA_256_LENGTH = 32;

    public byte[] serializeUnsigned(BinaryManifest manifest) {
        if (manifest == null) {
            throw new IllegalArgumentException("Binary manifest is required");
        }
        if (manifest.entries() == null) {
            throw new IllegalArgumentException("Manifest entries are required");
        }
        if (manifest.exportType() == null) {
            throw new IllegalArgumentException("Export type is required");
        }
        if (manifest.dataSha256() == null || manifest.dataSha256().length != SHA_256_LENGTH) {
            throw new IllegalArgumentException("dataSha256 must have length 32");
        }
        if (manifest.recordCount() != manifest.entries().size()) {
            throw new IllegalArgumentException("recordCount does not match entries size");
        }

        BinaryProtocolWriter writer = new BinaryProtocolWriter();
        writer.writeFixedAscii(manifest.magic(), MAGIC_LENGTH);
        writer.writeU16(manifest.version());
        writer.writeU8(manifest.exportType().getCode());
        writer.writeI64(manifest.generatedAtEpochMillis());
        writer.writeI64(manifest.sinceEpochMillis());
        writer.writeU32(manifest.recordCount());
        writer.writeBytes(manifest.dataSha256());

        for (BinaryManifestEntry entry : manifest.entries()) {
            serializeEntry(writer, entry);
        }

        return writer.toByteArray();
    }

    public byte[] appendSignature(byte[] unsignedManifest, byte[] manifestSignature) {
        if (unsignedManifest == null || unsignedManifest.length == 0) {
            throw new IllegalArgumentException("Unsigned manifest is empty");
        }
        if (manifestSignature == null || manifestSignature.length == 0) {
            throw new IllegalArgumentException("Manifest signature is empty");
        }

        BinaryProtocolWriter writer = new BinaryProtocolWriter();
        writer.writeBytes(unsignedManifest);
        writer.writeU32(manifestSignature.length);
        writer.writeBytes(manifestSignature);
        return writer.toByteArray();
    }

    private void serializeEntry(BinaryProtocolWriter writer, BinaryManifestEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Manifest entry is required");
        }
        if (entry.recordSignatureBytes() == null) {
            throw new IllegalArgumentException("Manifest entry signature is required");
        }

        writer.writeUuid(entry.id());
        writer.writeU8(entry.statusCode());
        writer.writeI64(entry.updatedAtEpochMillis());
        writer.writeU64(entry.dataOffset());
        writer.writeU32(entry.dataLength());
        writer.writeU32(entry.recordSignatureBytes().length);
        writer.writeBytes(entry.recordSignatureBytes());
    }
}
