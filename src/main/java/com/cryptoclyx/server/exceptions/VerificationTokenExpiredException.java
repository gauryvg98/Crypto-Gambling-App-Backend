package com.cryptoclyx.server.exceptions;

public class VerificationTokenExpiredException extends RuntimeException{

    public VerificationTokenExpiredException(String message) {
        super(message);
    }
}
