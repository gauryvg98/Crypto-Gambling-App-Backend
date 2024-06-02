package com.cryptoclyx.server.exceptions;

public class TransactionLogNotFoundException extends RuntimeException{

    public TransactionLogNotFoundException(String message) {
        super(message);
    }
}
