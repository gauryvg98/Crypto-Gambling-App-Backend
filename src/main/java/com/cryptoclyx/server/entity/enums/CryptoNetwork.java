package com.cryptoclyx.server.entity.enums;

public enum CryptoNetwork {
    SOLANA("SOLANA"),
    ETH("ETH");

    private final String value;

    CryptoNetwork(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
