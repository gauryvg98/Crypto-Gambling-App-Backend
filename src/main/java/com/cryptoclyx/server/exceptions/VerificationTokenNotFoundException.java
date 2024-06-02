package com.cryptoclyx.server.exceptions;

public class VerificationTokenNotFoundException extends RuntimeException{

    public VerificationTokenNotFoundException(String message) {
        super(message);
    }
}
