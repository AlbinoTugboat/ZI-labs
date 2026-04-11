package com.example.informationprotection.service.signature.binary;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class BinaryProtocolWriter {

    private static final long UINT32_MAX = 0xFFFF_FFFFL;
    private static final long UINT64_MIN = 0L;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final DataOutputStream output = new DataOutputStream(buffer);

    void writeFixedAscii(byte[] value, int expectedLength) {
        requireNonNull(value, "value");
        if (value.length != expectedLength) {
            throw new IllegalArgumentException("Expected ASCII field length " + expectedLength + ", got " + value.length);
        }
        writeBytes(value);
    }

    void writeU8(int value) {
        if (value < 0 || value > 0xFF) {
            throw new IllegalArgumentException("uint8 out of range: " + value);
        }
        io(() -> output.writeByte(value));
    }

    void writeU16(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("uint16 out of range: " + value);
        }
        io(() -> output.writeShort(value));
    }

    void writeU32(long value) {
        if (value < 0 || value > UINT32_MAX) {
            throw new IllegalArgumentException("uint32 out of range: " + value);
        }
        io(() -> output.writeInt((int) value));
    }

    void writeU64(long value) {
        if (value < UINT64_MIN) {
            throw new IllegalArgumentException("uint64 out of range: " + value);
        }
        io(() -> output.writeLong(value));
    }

    void writeI64(long value) {
        io(() -> output.writeLong(value));
    }

    void writeUuid(UUID value) {
        requireNonNull(value, "uuid");
        writeI64(value.getMostSignificantBits());
        writeI64(value.getLeastSignificantBits());
    }

    void writeUtf8WithU32Length(String value) {
        requireNonNull(value, "string");
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeBytesWithU32Length(bytes);
    }

    void writeBytesWithU32Length(byte[] value) {
        requireNonNull(value, "bytes");
        writeU32(value.length);
        writeBytes(value);
    }

    void writeBytes(byte[] value) {
        requireNonNull(value, "bytes");
        io(() -> output.write(value));
    }

    byte[] toByteArray() {
        return buffer.toByteArray();
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    private void io(IoOperation operation) {
        try {
            operation.run();
        } catch (IOException ex) {
            throw new IllegalStateException("Binary serialization failed", ex);
        }
    }

    @FunctionalInterface
    private interface IoOperation {
        void run() throws IOException;
    }
}
