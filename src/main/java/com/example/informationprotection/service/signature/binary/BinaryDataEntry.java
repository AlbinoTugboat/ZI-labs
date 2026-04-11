package com.example.informationprotection.service.signature.binary;

public record BinaryDataEntry(
        String threatName,
        byte[] firstBytes,
        byte[] remainderHash,
        long remainderLength,
        String fileType,
        long offsetStart,
        long offsetEnd
) {
}
