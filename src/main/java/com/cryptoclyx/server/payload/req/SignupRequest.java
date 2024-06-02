package com.cryptoclyx.server.payload.req;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class SignupRequest {

    private String firstName;

    private String lastName;

    @Length(message = "Min nickname length is 4 symbols", min = 4)
    private String nickname;

    @Email(message = "Email is not correct")
    private String email;

    private String phoneNumber;

    @Length(message = "Min password length is 5 symbols", min = 5)
    private String password;

}
