package com.cryptoclyx.server.payload.req;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class ChangePasswordRequest {

    @Length(message = "Min length 3", min = 3)
    private String verificationToken;

    @Length(message = "Min length 5 symbols", min = 5)
    private String newPassword;

}
