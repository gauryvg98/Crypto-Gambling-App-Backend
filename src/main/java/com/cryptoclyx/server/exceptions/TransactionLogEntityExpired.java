package com.cryptoclyx.server.exceptions;

public class TransactionLogEntityExpired extends RuntimeException{

    public TransactionLogEntityExpired(String message) {
        super(message);
    }
}
