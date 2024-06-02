package com.cryptoclyx.server.exceptions;

public class TemporaryWithdrawalUnavailableException extends RuntimeException{

    public TemporaryWithdrawalUnavailableException(String message) {
        super(message);
    }
}
