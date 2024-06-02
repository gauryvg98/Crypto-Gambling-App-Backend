package com.cryptoclyx.server.payload.req;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;

@Getter
@Setter
public class SigninRequest {

    @Email(message = "Email is not correct")
    private String email;

    private String password;
}
