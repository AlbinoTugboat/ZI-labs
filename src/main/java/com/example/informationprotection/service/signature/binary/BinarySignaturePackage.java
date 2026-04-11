package com.example.informationprotection.service.signature.binary;

public record BinarySignaturePackage(
        byte[] manifestBytes,
        byte[] dataBytes
) {
}
