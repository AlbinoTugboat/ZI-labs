package com.example.informationprotection.service.signature.binary;

import java.util.List;

public record BinaryDataSerializationResult(
        byte[] dataBytes,
        List<BinaryDataEntryRange> entryRanges
) {
}
