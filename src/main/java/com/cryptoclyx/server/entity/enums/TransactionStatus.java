package com.cryptoclyx.server.entity.enums;

public enum TransactionStatus {
    USER_TRANSFERS_MONEY("USER_TRANSFERS_MONEY"),
    CONFIRMING_USER_TRANSACTION("CONFIRMING_USER_TRANSACTION"),
    EXPIRED("EXPIRED"),
    INCORRECT_TRANSFER_BALANCE("INCORRECT_TRANSFER_BALANCE"),
    TOP_UP_CANCELED_BY_USER("TOP_UP_CANCELED_BY_USER"),

    WITHDRAW_REQUESTED("WITHDRAW_REQUESTED"),
    WITHDRAW_REQ_SENT_TO_NETWORK("WITHDRAW_REQ_SENT_TO_NETWORK"),
    WITHDRAW_IN_PROGRESS("WITHDRAW_IN_PROGRESS"),
    WITHDRAW_CANCELED_BY_USER("WITHDRAW_CANCELED_BY_USER"),

    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}