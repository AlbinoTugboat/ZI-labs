package com.example.informationprotection.service.signature.binary;

import com.example.informationprotection.dto.signature.MalwareSignatureResponse;
import com.example.informationprotection.entity.signature.SignatureStatus;
import com.example.informationprotection.service.license.TicketSignatureService;
import com.example.informationprotection.service.signature.MalwareSignatureService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class BinarySignaturePackageService {

    private static final byte[] MANIFEST_MAGIC = "MF-Churakov-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] DATA_MAGIC = "DB-Churakov-".getBytes(StandardCharsets.US_ASCII);
    private static final int FORMAT_VERSION = 1;
    private static final long SINCE_NOT_APPLICABLE = -1L;

    private final MalwareSignatureService malwareSignatureService;
    private final BinaryDataSerializer binaryDataSerializer;
    private final BinaryManifestSerializer binaryManifestSerializer;
    private final TicketSignatureService ticketSignatureService;

    public BinarySignaturePackageService(
            MalwareSignatureService malwareSignatureService,
            BinaryDataSerializer binaryDataSerializer,
            BinaryManifestSerializer binaryManifestSerializer,
            TicketSignatureService ticketSignatureService
    ) {
        this.malwareSignatureService = malwareSignatureService;
        this.binaryDataSerializer = binaryDataSerializer;
        this.binaryManifestSerializer = binaryManifestSerializer;
        this.ticketSignatureService = ticketSignatureService;
    }

    public BinarySignaturePackage buildFullBasePackage() {
        List<MalwareSignatureResponse> signatures = malwareSignatureService.getFullBase();
        return buildPackage(signatures, BinaryExportType.FULL_BASE, SINCE_NOT_APPLICABLE);
    }

    public BinarySignaturePackage buildIncrementPackage(Instant since) {
        List<MalwareSignatureResponse> signatures = malwareSignatureService.getIncrement(since);
        return buildPackage(signatures, BinaryExportType.INCREMENT, since.toEpochMilli());
    }

    public BinarySignaturePackage buildByIdsPackage(List<UUID> ids) {
        List<MalwareSignatureResponse> signatures = malwareSignatureService.getByIds(ids);
        return buildPackage(signatures, BinaryExportType.BY_IDS, SINCE_NOT_APPLICABLE);
    }

    private BinarySignaturePackage buildPackage(
            List<MalwareSignatureResponse> signatures,
            BinaryExportType exportType,
            long sinceEpochMillis
    ) {
        if (signatures == null) {
            throw new IllegalArgumentException("signatures are required");
        }

        Instant generatedAt = Instant.now();
        BinaryDataDocument dataDocument = new BinaryDataDocument(
                DATA_MAGIC,
                FORMAT_VERSION,
                signatures.stream().map(this::toDataEntry).toList()
        );
        BinaryDataSerializationResult dataResult = binaryDataSerializer.serialize(dataDocument);

        List<BinaryManifestEntry> manifestEntries = buildManifestEntries(signatures, dataResult.entryRanges());
        BinaryManifest manifest = new BinaryManifest(
                MANIFEST_MAGIC,
                FORMAT_VERSION,
                exportType,
                generatedAt.toEpochMilli(),
                sinceEpochMillis,
                manifestEntries.size(),
                sha256(dataResult.dataBytes()),
                manifestEntries
        );

        byte[] unsignedManifest = binaryManifestSerializer.serializeUnsigned(manifest);
        byte[] manifestSignature = ticketSignatureService.signRaw(unsignedManifest);
        byte[] signedManifest = binaryManifestSerializer.appendSignature(unsignedManifest, manifestSignature);
        return new BinarySignaturePackage(signedManifest, dataResult.dataBytes());
    }

    private List<BinaryManifestEntry> buildManifestEntries(
            List<MalwareSignatureResponse> signatures,
            List<BinaryDataEntryRange> ranges
    ) {
        if (signatures.size() != ranges.size()) {
            throw new IllegalStateException("Serialized data range count does not match signature count");
        }

        List<BinaryManifestEntry> entries = new ArrayList<>(signatures.size());
        for (int i = 0; i < signatures.size(); i++) {
            MalwareSignatureResponse signature = signatures.get(i);
            BinaryDataEntryRange range = ranges.get(i);
            byte[] recordSignature = decodeBase64(signature.getDigitalSignatureBase64(), "digitalSignatureBase64");
            entries.add(new BinaryManifestEntry(
                    signature.getId(),
                    mapStatusCode(signature.getStatus()),
                    signature.getUpdatedAt().toEpochMilli(),
                    range.dataOffset(),
                    range.dataLength(),
                    recordSignature
            ));
        }
        return entries;
    }

    private BinaryDataEntry toDataEntry(MalwareSignatureResponse signature) {
        if (signature == null) {
            throw new IllegalArgumentException("signature entry is required");
        }
        return new BinaryDataEntry(
                require(signature.getThreatName(), "threatName"),
                decodeHex(signature.getFirstBytesHex(), "firstBytesHex"),
                decodeHex(signature.getRemainderHashHex(), "remainderHashHex"),
                require(signature.getRemainderLength(), "remainderLength"),
                require(signature.getFileType(), "fileType"),
                require(signature.getOffsetStart(), "offsetStart"),
                require(signature.getOffsetEnd(), "offsetEnd")
        );
    }

    private byte[] decodeHex(String value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Missing value in field " + fieldName);
        }
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid hex value in field " + fieldName, ex);
        }
    }

    private byte[] decodeBase64(String value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Missing value in field " + fieldName);
        }
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid Base64 value in field " + fieldName, ex);
        }
    }

    private byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private int mapStatusCode(SignatureStatus status) {
        if (status == SignatureStatus.ACTUAL) {
            return 1;
        }
        if (status == SignatureStatus.DELETED) {
            return 2;
        }
        throw new IllegalStateException("Unsupported signature status: " + status);
    }

    private <T> T require(T value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Missing value in field " + fieldName);
        }
        return value;
    }
}
