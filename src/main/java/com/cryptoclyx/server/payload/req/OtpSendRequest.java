package com.cryptoclyx.server.payload.req;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OtpSendRequest {
    private String token;
    private String phoneNumber;
}
