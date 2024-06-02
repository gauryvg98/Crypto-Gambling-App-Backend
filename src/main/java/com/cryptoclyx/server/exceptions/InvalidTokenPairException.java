package com.cryptoclyx.server.exceptions;

public class InvalidTokenPairException extends RuntimeException{

    public InvalidTokenPairException(String message) {
        super(message);
    }
}
