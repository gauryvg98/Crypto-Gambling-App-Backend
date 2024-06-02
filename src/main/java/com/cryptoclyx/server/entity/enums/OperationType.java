package com.cryptoclyx.server.entity.enums;

public enum OperationType {
    TOP_UP("TOP_UP"),
    WITHDRAW("WITHDRAW");

    private final String value;

    OperationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
