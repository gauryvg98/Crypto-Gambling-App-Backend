package com.cryptoclyx.server.exceptions;

public class PasswordDoesNotMatchException extends RuntimeException{

    public PasswordDoesNotMatchException(String message) {
        super(message);
    }
}
