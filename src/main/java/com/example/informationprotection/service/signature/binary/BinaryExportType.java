package com.example.informationprotection.service.signature.binary;

public enum BinaryExportType {
    FULL_BASE(1),
    INCREMENT(2),
    BY_IDS(3);

    private final int code;

    BinaryExportType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
