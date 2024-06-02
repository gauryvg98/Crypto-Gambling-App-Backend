package com.cryptoclyx.server.exceptions;

import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PhoneNumberConfirmationRequired extends RuntimeException{

    private AuthTokenResponse token;

    public PhoneNumberConfirmationRequired(String message) {
        super(message);
    }

    public PhoneNumberConfirmationRequired(String message, AuthTokenResponse token) {
        super(message);
        this.token = token;
    }
}
