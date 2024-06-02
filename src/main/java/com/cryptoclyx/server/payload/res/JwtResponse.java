package com.cryptoclyx.server.payload.res;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Calendar;
import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {

    private String email;

    private String accessToken;

    private String refreshToken;

    private Calendar expiresAt;

    private String roles;
}