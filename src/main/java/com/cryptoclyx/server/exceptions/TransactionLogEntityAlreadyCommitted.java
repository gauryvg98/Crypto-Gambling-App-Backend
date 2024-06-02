package com.cryptoclyx.server.exceptions;

public class TransactionLogEntityAlreadyCommitted extends RuntimeException{

    public TransactionLogEntityAlreadyCommitted(String message) {
        super(message);
    }
}
