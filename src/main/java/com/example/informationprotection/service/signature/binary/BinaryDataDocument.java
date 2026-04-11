package com.example.informationprotection.service.signature.binary;

import java.util.List;

public record BinaryDataDocument(
        byte[] magic,
        int version,
        List<BinaryDataEntry> entries
) {
    public long recordCount() {
        return entries.size();
    }
}
