package com.cryptoclyx.server.payload.res;

import com.cryptoclyx.server.payload.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class AuthTokenResponse {

    private String token;

    private Date expirationDate;

    private UserDto user;

    @JsonIgnore
    private String otp;
}
