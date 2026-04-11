package com.example.informationprotection.service.signature.binary;

import com.example.informationprotection.dto.signature.MalwareSignatureResponse;
import com.example.informationprotection.entity.signature.SignatureStatus;
import com.example.informationprotection.service.license.TicketSignatureService;
import com.example.informationprotection.service.signature.MalwareSignatureService;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BinarySignaturePackageServiceTest {

    @Test
    void shouldBuildFullBaseBinaryPackage() {
        MalwareSignatureService signatureService = mock(MalwareSignatureService.class);
        TicketSignatureService ticketSignatureService = mock(TicketSignatureService.class);
        BinarySignaturePackageService binaryPackageService = new BinarySignaturePackageService(
                signatureService,
                new BinaryDataSerializer(),
                new BinaryManifestSerializer(),
                ticketSignatureService
        );

        MalwareSignatureResponse signature = signature(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                Instant.parse("2026-04-02T10:15:30Z"),
                SignatureStatus.ACTUAL
        );
        when(signatureService.getFullBase()).thenReturn(List.of(signature));
        when(ticketSignatureService.signRaw(any())).thenReturn(new byte[]{0x11, 0x22, 0x33});

        BinarySignaturePackage binaryPackage = binaryPackageService.buildFullBasePackage();

        assertNotNull(binaryPackage);
        assertArrayEquals("DB-Churakov-".getBytes(StandardCharsets.US_ASCII), slice(binaryPackage.dataBytes(), 0, 12));
        assertArrayEquals("MF-Churakov-".getBytes(StandardCharsets.US_ASCII), slice(binaryPackage.manifestBytes(), 0, 12));

        ByteBuffer manifestBuffer = ByteBuffer.wrap(binaryPackage.manifestBytes()).order(ByteOrder.BIG_ENDIAN);
        assertEquals(1, manifestBuffer.getShort(12));
        assertEquals(BinaryExportType.FULL_BASE.getCode(), manifestBuffer.get(14));
        assertEquals(-1L, manifestBuffer.getLong(23));
        assertEquals(1L, Integer.toUnsignedLong(manifestBuffer.getInt(31)));

        int recordSignatureLength = manifestBuffer.getInt(104);
        assertEquals(4, recordSignatureLength);
        long manifestSignatureLength = Integer.toUnsignedLong(manifestBuffer.getInt(112));
        assertEquals(3L, manifestSignatureLength);
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33}, slice(binaryPackage.manifestBytes(), 116, 119));

        verify(ticketSignatureService).signRaw(any());
    }

    @Test
    void shouldWriteSinceToManifestForIncrementExport() {
        MalwareSignatureService signatureService = mock(MalwareSignatureService.class);
        TicketSignatureService ticketSignatureService = mock(TicketSignatureService.class);
        BinarySignaturePackageService binaryPackageService = new BinarySignaturePackageService(
                signatureService,
                new BinaryDataSerializer(),
                new BinaryManifestSerializer(),
                ticketSignatureService
        );

        Instant since = Instant.parse("2026-04-01T00:00:00Z");
        when(signatureService.getIncrement(since)).thenReturn(List.of());
        when(ticketSignatureService.signRaw(any())).thenReturn(new byte[]{0x55});

        BinarySignaturePackage binaryPackage = binaryPackageService.buildIncrementPackage(since);
        ByteBuffer manifestBuffer = ByteBuffer.wrap(binaryPackage.manifestBytes()).order(ByteOrder.BIG_ENDIAN);

        assertEquals(BinaryExportType.INCREMENT.getCode(), manifestBuffer.get(14));
        assertEquals(since.toEpochMilli(), manifestBuffer.getLong(23));
        assertEquals(0L, Integer.toUnsignedLong(manifestBuffer.getInt(31)));
    }

    private MalwareSignatureResponse signature(UUID id, Instant updatedAt, SignatureStatus status) {
        return new MalwareSignatureResponse(
                id,
                "Test.Threat",
                "A1B2",
                "0A0B0C",
                12L,
                "exe",
                5L,
                50L,
                updatedAt,
                status,
                Base64.getEncoder().encodeToString(new byte[]{0x01, 0x02, 0x03, 0x04})
        );
    }

    private byte[] slice(byte[] source, int fromInclusive, int toExclusive) {
        byte[] result = new byte[toExclusive - fromInclusive];
        System.arraycopy(source, fromInclusive, result, 0, result.length);
        return result;
    }
}
