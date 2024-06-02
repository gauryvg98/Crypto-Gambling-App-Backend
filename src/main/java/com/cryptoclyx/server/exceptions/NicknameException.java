package com.cryptoclyx.server.exceptions;


public class NicknameException extends RuntimeException {

    public NicknameException(String message) {
        super(message);
    }

    public NicknameException(String message, Throwable cause) {
        super(message, cause);
    }
}
