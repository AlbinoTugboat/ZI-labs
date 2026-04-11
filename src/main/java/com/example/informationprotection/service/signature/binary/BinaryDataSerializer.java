package com.example.informationprotection.service.signature.binary;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BinaryDataSerializer {

    private static final int MAGIC_LENGTH = 12;

    public BinaryDataSerializationResult serialize(BinaryDataDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Binary data document is required");
        }
        if (document.entries() == null) {
            throw new IllegalArgumentException("Data entries are required");
        }

        BinaryProtocolWriter writer = new BinaryProtocolWriter();
        writer.writeFixedAscii(document.magic(), MAGIC_LENGTH);
        writer.writeU16(document.version());
        writer.writeU32(document.recordCount());

        List<BinaryDataEntryRange> ranges = new ArrayList<>(document.entries().size());
        long currentOffset = 0L;

        for (BinaryDataEntry entry : document.entries()) {
            byte[] encodedEntry = serializeEntry(entry);
            ranges.add(new BinaryDataEntryRange(currentOffset, encodedEntry.length));
            writer.writeBytes(encodedEntry);
            currentOffset += encodedEntry.length;
        }

        return new BinaryDataSerializationResult(writer.toByteArray(), ranges);
    }

    private byte[] serializeEntry(BinaryDataEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Data entry is required");
        }
        if (entry.offsetEnd() < entry.offsetStart()) {
            throw new IllegalArgumentException("offsetEnd must be >= offsetStart");
        }

        BinaryProtocolWriter writer = new BinaryProtocolWriter();
        writer.writeUtf8WithU32Length(entry.threatName());
        writer.writeBytesWithU32Length(entry.firstBytes());
        writer.writeBytesWithU32Length(entry.remainderHash());
        writer.writeI64(entry.remainderLength());
        writer.writeUtf8WithU32Length(entry.fileType());
        writer.writeI64(entry.offsetStart());
        writer.writeI64(entry.offsetEnd());
        return writer.toByteArray();
    }
}
